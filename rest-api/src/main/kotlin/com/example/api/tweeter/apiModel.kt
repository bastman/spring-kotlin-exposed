package com.example.api.tweeter

import com.example.api.tweeter.db.TweetsRecord
import java.time.Instant
import java.util.*

data class CreateTweetRequest(val message: String, val comment: String?)
data class UpdateTweetRequest(val message: String, val comment: String?)

data class TweetDto(
        val id: UUID, val createdAt: Instant, val modifiedAt: Instant,
        val message: String, val comment: String?
)

fun TweetsRecord.toTweetsDto() = TweetDto(
        id = id, createdAt = createdAt, modifiedAt = modifiedAt,
        message = message, comment = comment
)

fun CreateTweetRequest.toRecord(id: UUID, now: Instant): TweetsRecord =
        TweetsRecord(
                id = id, version = 0, createdAt = now, modifiedAt = now,
                message = message, comment = comment
        )
