package com.example.api.tweeter

import com.example.api.tweeter.db.TweetStatus
import com.example.api.tweeter.db.TweetsRecord
import com.example.api.tweeter.db.TweetsRepo
import com.example.api.tweeter.db.TweetsTable
import com.example.api.tweeter.search.TweeterSearchHandler
import com.example.api.tweeter.search.TweeterSearchRequest
import com.example.api.tweeter.search.TweeterSearchResponse
import mu.KLogging
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

    @PostMapping("/api/tweeter/search")
    fun search(@RequestBody payload: TweeterSearchRequest): TweeterSearchResponse = payload
            .let(search::handle)

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



