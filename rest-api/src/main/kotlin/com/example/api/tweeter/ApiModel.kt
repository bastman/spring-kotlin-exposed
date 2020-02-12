package com.example.api.tweeter

import am.ik.yavi.builder.ValidatorBuilder
import am.ik.yavi.builder.konstraint
import am.ik.yavi.core.ConstraintViolations
import am.ik.yavi.core.Validator
import com.example.api.common.rest.serialization.Patchable
import com.example.api.tweeter.db.TweetStatus
import com.example.api.tweeter.db.TweetsRecord
import java.time.Instant
import java.util.*

data class CreateTweetRequest(
        val message: String,
        val comment: String?
) {
    companion object
}

data class UpdateTweetRequest(
        val message: String,
        val comment: String?
)

data class PatchTweetRequest(
        val message: Patchable<String>,
        val comment: Patchable<String?>,
        val status: Patchable<TweetStatus>
)

data class TweetDto(
        val id: UUID, val createdAt: Instant, val modifiedAt: Instant, val deletedAt: Instant, val version: Int,
        val message: String, val comment: String?, val status: TweetStatus
)

fun TweetsRecord.toTweetsDto() = TweetDto(
        id = id, createdAt = createdAt, modifiedAt = modifiedAt, deletedAt = deletedAt, version = version,
        message = message, comment = comment, status = status
)

fun CreateTweetRequest.toRecord(id: UUID, now: Instant): TweetsRecord =
        TweetsRecord(
                id = id, version = 0, createdAt = now, modifiedAt = now, deletedAt = Instant.EPOCH,
                message = message, comment = comment, status = TweetStatus.DRAFT
        )


fun CreateTweetRequest.Companion.validatorBuilder(): ValidatorBuilder<CreateTweetRequest> = ValidatorBuilder.of()
fun CreateTweetRequest.Companion.validator(): Validator<CreateTweetRequest> = validatorBuilder()
        .konstraint(CreateTweetRequest::message) {
            notNull()
                    .lessThanOrEqual(20)
        }
        .konstraint(CreateTweetRequest::comment) {
            lessThanOrEqual(30)
        }
        .build()

fun CreateTweetRequest.validate(): ConstraintViolations = CreateTweetRequest
        .validator()
        .validate(this)

