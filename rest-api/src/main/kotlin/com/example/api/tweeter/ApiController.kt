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

    @GetMapping("$BASE_URI/{id}")
    @Transactional(readOnly = true)
    fun getOne(@PathVariable id: UUID): TweetDto = repo[id]
            .toTweetsDto()

    @GetMapping(BASE_URI)
    @Transactional(readOnly = true)
    fun findAll(): List<TweetDto> = repo
            .findAll()
            .map { it.toTweetsDto() }

    @GetMapping("$BASE_URI/distinctOn")
    @Transactional(readOnly = true)
    fun findDistinct(): List<TweetDto> {
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

    @PutMapping(BASE_URI)
    @Transactional(readOnly = false)
    fun createOne(@RequestBody req: CreateTweetRequest): TweetDto = req
            .also {
                it
                        .validate()
                        .throwIfInvalid { violations ->
                            val errors = violations
                                    .map { v -> v.message() }
                                    .joinToString(separator = "\n")
                            BadRequestException("Check your payload ! some errors here. errors: $errors")
                        }
            }
            .toRecord(id = UUID.randomUUID(), now = Instant.now())
            .let(repo::insert)
            .also { logger.info { "INSERT DB ENTITY: $it" } }
            .toTweetsDto()

    @PostMapping("$BASE_URI/{id}")
    @Transactional(readOnly = false)
    fun updateOne(@PathVariable id: UUID, @RequestBody req: UpdateTweetRequest): TweetDto = repo[id]
            .copy(modifiedAt = Instant.now(), message = req.message, comment = req.comment)
            .let(repo::update)
            .also { logger.info { "UPDATE DB ENTITY: $it" } }
            .toTweetsDto()

    @PatchMapping("$BASE_URI/{id}")
    @Transactional(readOnly = false)
    fun patchOne(
            @PathVariable id: UUID,
            @RequestBody req: PatchTweetRequest
    ): TweetDto {
        val sourceRecord: TweetsRecord = repo[id]
        val patchedRecord: TweetsRecord = sourceRecord
                .patchMessage(patch = req.message)
                .patchComment(patch = req.comment)
                .patchStatus(patch = req.status)

        val resultRecord: TweetsRecord = when (patchedRecord == sourceRecord) {
            true -> sourceRecord
                    .also { logger.info { "Nothing to patch. Nothing to update: $it" } }
            false -> patchedRecord
                    .copy(modifiedAt = Instant.now())
                    .also { logger.info { "DB Update ... to: $it from: $sourceRecord" } }
                    .let(repo::update)
        }
        return resultRecord.toTweetsDto()
    }

    @PostMapping("/api/tweeter/search")
    @Transactional(readOnly = true)
    fun search(@RequestBody payload: TweeterSearchRequest): TweeterSearchResponse = payload
            .let(search::handle)


    // example: "jmesPath": "items[0:2].{id:id, createdAt:createdAt}"
    @PostMapping("$BASE_URI/search/jmespath")
    @ApiResponses(
            value = [
                ApiResponse(code = 200, response = TweeterSearchResponse::class, message = "some response")
            ]
    )
    fun searchJMESPath(@RequestBody payload: TweeterSearchRequest): SearchJMESPathResponse {
        val sink: SearchJMESPathResponse = when (payload.jmesPath.isNullOrBlank()) {
            true -> {
                val r: TweeterSearchResponse = search.handle(payload)
                SearchJMESPathResponse.Raw(r)
            }
            false -> {
                val expression: Expression<JsonNode> = try {
                    JMESPATH.compile(payload.jmesPath)
                } catch (all: Exception) {
                    throw BadRequestException("Invalid req.jmesPath ! ${all.message}")
                }
                val data: TweeterSearchResponse = search.handle(payload)
                val json: String = JSON.writeValueAsString(data)
                val tree: JsonNode = JSON.readTree(json)
                val r: JsonNode = expression.search(tree)
                SearchJMESPathResponse.JMESPath(r)
            }
        }
        return sink
    }

    @PutMapping("$BASE_URI/bulk-generate/{maxRecords}")
    @Transactional(readOnly = false)
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

    companion object : KLogging() {
        private const val BASE_URI = "/api/tweeter"
    }
}

private val JSON = Jackson.defaultMapper()
private val JMESPATH: JmesPath<JsonNode> = JacksonRuntime()


// Note: springfox-swagger limitations: currently, no support for Response is "oneOf" (aka "union types")
// see: https://github.com/springfox/springfox/issues/2928
@ApiModel(subTypes = [SearchJMESPathResponse.Raw::class, SearchJMESPathResponse.JMESPath::class])
sealed class SearchJMESPathResponse {
    data class Raw(val data: TweeterSearchResponse) : SearchJMESPathResponse()
    data class JMESPath(val data: JsonNode) : SearchJMESPathResponse()
}


private fun TweetsRecord.patchMessage(patch: Patchable<String>): TweetsRecord = when (patch) {
    is Patchable.Present -> copy(message = patch.content)
    is Patchable.Null -> this
    is Patchable.Undefined -> this
}

private fun TweetsRecord.patchComment(patch: Patchable<String?>): TweetsRecord = when (patch) {
    is Patchable.Present -> copy(comment = patch.content)
    is Patchable.Null -> copy(comment = null) // record.comment:String? -> accept nulls
    is Patchable.Undefined -> this
}

private fun TweetsRecord.patchStatus(patch: Patchable<TweetStatus>): TweetsRecord = when (patch) {
    is Patchable.Present -> copy(status = patch.content)
    is Patchable.Null -> this
    is Patchable.Undefined -> this
}




