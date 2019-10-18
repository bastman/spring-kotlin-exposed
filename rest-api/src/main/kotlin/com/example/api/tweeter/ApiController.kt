package com.example.api.tweeter

import com.example.api.common.rest.error.exception.BadRequestException
import com.example.api.common.rest.serialization.Patchable
import com.example.api.tweeter.db.TweetStatus
import com.example.api.tweeter.db.TweetsRecord
import com.example.api.tweeter.db.TweetsRepo
import com.example.api.tweeter.db.TweetsTable
import com.example.api.tweeter.search.TweeterSearchHandler
import com.example.api.tweeter.search.TweeterSearchRequest
import com.example.api.tweeter.search.TweeterSearchResponse
import com.example.config.Jackson
import com.example.util.exposed.functions.postgres.distinctOn
import com.example.util.exposed.query.toSQL
import com.fasterxml.jackson.databind.JsonNode
import io.burt.jmespath.Expression
import io.burt.jmespath.JmesPath
import io.burt.jmespath.jackson.JacksonRuntime
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import mu.KLogging
import org.funktionale.option.Option
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
class TweeterApiController(
        private val repo: TweetsRepo,
        private val search: TweeterSearchHandler
) {

    @GetMapping("/api/tweeter")
    fun findAll(): List<TweetDto> = repo.findAll().map { it.toTweetsDto() }

    @GetMapping("/api/tweeter/{id}")
    fun getOne(@PathVariable id: UUID): TweetDto =
            repo[id].toTweetsDto()

    @GetMapping("/api/tweeter/distinctOn")
    @Transactional(readOnly = true)
    fun getDistinct(): List<TweetDto> {
        val selectDistinctOn: CustomFunction<Boolean?> = distinctOn(
                TweetsTable.message,
                TweetsTable.comment
        )
        val selectColumns: List<Column<*>> = (TweetsTable.columns)
        val selectWhere: Op<Boolean> = (
                TweetsTable.createdAt.greaterEq(Instant.EPOCH)
                )

        val query: Query = TweetsTable
                .slice(
                        selectDistinctOn,
                        *selectColumns.toTypedArray()
                )
                .select { selectWhere }

        val SQL: String = query.toSQL()
        logger.info { "==== Select Distinct On Example === SQL: $SQL " }
        val dtos: List<TweetDto> = query
                .map { with(TweetsTable) { it.toTweetsRecord() } }
                .map { it.toTweetsDto() }
        return dtos
    }

    @PutMapping("/api/tweeter")
    fun createOne(@RequestBody req: CreateTweetRequest): TweetDto =
            req.toRecord(id = UUID.randomUUID(), now = Instant.now())
                    .let { repo.insert(it) }
                    .also { logger.info { "INSERT DB ENTITY: $it" } }
                    .toTweetsDto()

    @PostMapping("/api/tweeter/{id}")
    fun updateOne(@PathVariable id: UUID, @RequestBody req: UpdateTweetRequest): TweetDto =
            repo[id]
                    .copy(modifiedAt = Instant.now(), message = req.message, comment = req.comment)
                    .let { repo.update(it) }
                    .also { logger.info { "UPDATE DB ENTITY: $it" } }
                    .toTweetsDto()


    @PatchMapping("/api/tweeter/{id}")
    fun patchOne(
            @PathVariable id: UUID,
            @RequestBody req: PatchTweetRequest
    ): TweetDto {
        val record: TweetsRecord = repo[id]
        val message: Option<String> = when (val it = req.message) {
            is Patchable.Null -> Option.None // Option.Some<String?>(null)
            is Patchable.Undefined -> Option.None
            is Patchable.Present -> Option.Some(it.content)
        }
        val comment: Option<String> = when (val it = req.comment) {
            is Patchable.Null -> Option.None // Option.Some<String?>(null)
            is Patchable.Undefined -> Option.None
            is Patchable.Present -> Option.Some(it.content)
        }
        return record.let {
            when (message) {
                is Option.Some -> it.copy(message = message.get(), modifiedAt = Instant.now())
                is Option.None -> it
            }
        }.let {
            when (comment) {
                is Option.Some -> it.copy(comment = comment.get(), modifiedAt = Instant.now())
                is Option.None -> it
            }
        }.let(repo::update)
                .also { logger.info { "UPDATE DB ENTITY: $it" } }
                .toTweetsDto()
    }

    @PostMapping("/api/tweeter/search")
    fun search(@RequestBody payload: TweeterSearchRequest): TweeterSearchResponse = payload
            .let(search::handle)


    // example: "jq": "items[0:2].{id:id, createdAt:createdAt}"
    @PostMapping("/api/tweeter/search/jq")
    @ApiResponses(
            value = [
                ApiResponse(code = 200, response = TweeterSearchResponse::class, message = "some response")
            ]
    )
    fun searchJQ(@RequestBody payload: TweeterSearchRequest): SearchJqResponse {
        val sink: SearchJqResponse = when (payload.jq.isNullOrBlank()) {
            true -> {
                val r: TweeterSearchResponse = search.handle(payload)
                SearchJqResponse.Raw(r)
            }
            false -> {
                val expression: Expression<JsonNode> = try {
                    JMESPATH.compile(payload.jq)
                } catch (all: Exception) {
                    throw BadRequestException("Invalid req.jq ! ${all.message}")
                }
                val data: TweeterSearchResponse = search.handle(payload)
                val json: String = JSON.writeValueAsString(data)
                val tree: JsonNode = JSON.readTree(json)
                val r: JsonNode = expression.search(tree)
                SearchJqResponse.Jq(r)
            }
        }
        return sink
    }

    @PutMapping("/api/tweeter/bulk-generate/{maxRecords}")
    fun bulkGenerate(@PathVariable maxRecords: Int): Any {
        val words: List<String> = "The quick brown fox jumps over the lazy dog".split(" ")
        val records: List<TweetsRecord> = (0..maxRecords).map {
            val now: Instant = Instant.now()
            TweetsRecord(
                    id = UUID.randomUUID(),
                    createdAt = now,
                    modifiedAt = now,
                    deletedAt = Instant.EPOCH,
                    status = TweetStatus.values().random(),
                    comment = "comment: ${words.shuffled().take(3).joinToString(separator = " ")}",
                    message = "message: ${words.shuffled().take(5).joinToString(separator = " ")}",
                    version = (0..10).random()

            ).let(repo::insert)
        }
        return mapOf(
                "items" to records.map { with(TweetsTable) { it.toTweetsDto() } }
        )
    }

    companion object : KLogging()
}

private val JSON = Jackson.defaultMapper()
private val JMESPATH: JmesPath<JsonNode> = JacksonRuntime()


// Note: springfox-swagger limitations: currently, no support for Response is "oneOf" (aka "union types")
// see: https://github.com/springfox/springfox/issues/2928
@ApiModel(subTypes = [SearchJqResponse.Raw::class, SearchJqResponse.Jq::class])
sealed class SearchJqResponse {
    data class Raw(val data: TweeterSearchResponse) : SearchJqResponse()
    data class Jq(val data: JsonNode) : SearchJqResponse()
}





