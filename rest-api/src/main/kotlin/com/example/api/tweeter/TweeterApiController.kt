package com.example.api.tweeter


import com.example.api.tweeter.domain.Tweet
import com.example.api.tweeter.domain.TweetRepository
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
class TweeterApiController(private val tweeterRepo: TweetRepository) {

    @GetMapping("/api/tweeter")
    fun findAll() = tweeterRepo.findAll().map { it.toDto() }

    @GetMapping("/api/tweeter/{id}")
    fun getOne(@PathVariable id: UUID) =
            tweeterRepo.requireOneById(id).toDto()


    @PutMapping("/api/tweeter")
    fun createOne(@RequestBody req: CreateTweetRequest): TweetDto {
        val record = req.toRecord()
        tweeterRepo.insert(record)

        return tweeterRepo.requireOneById(record.id).toDto()
    }

    @PostMapping("/api/tweeter/{id}")
    fun updateOne(@PathVariable id: UUID, @RequestBody req: CreateTweetRequest): TweetDto {
        val record = tweeterRepo.requireOneById(id)
                .copy(
                        modifiedAt = Instant.now(),
                        message = req.message,
                        comment = req.comment
                )

        tweeterRepo.update(record)

        return tweeterRepo.requireOneById(record.id).toDto()
    }
}

data class CreateTweetRequest(val message: String, val comment: String?)

data class TweetDto(
        val id: UUID,
        val version: Int,
        val createdAt: Instant,
        val modifiedAt: Instant,
        val message: String,
        val comment: String?
)

private fun Tweet.toDto() = TweetDto(
        id = id,
        version = version,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        message = message,
        comment = comment
)

private fun CreateTweetRequest.toRecord(): Tweet {
    val now = Instant.now()
    return Tweet(
            id = UUID.randomUUID(),
            version = 0,
            createdAt = now,
            modifiedAt = now,
            message = message,
            comment = comment
    )
}

