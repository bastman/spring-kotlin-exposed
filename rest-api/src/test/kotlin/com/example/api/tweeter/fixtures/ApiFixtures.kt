package com.example.api.tweeter.fixtures

import com.example.api.tweeter.db.TweetStatus
import com.example.api.tweeter.db.TweetsRecord
import com.example.testutils.random.random
import com.example.testutils.random.randomEnumValue
import com.example.testutils.random.randomString
import java.time.Duration
import java.time.Instant
import java.util.*

object TweeterApiFixtures {
    fun newTweetsRecord(tweetsId:UUID= UUID.randomUUID(),now:Instant=Instant.now()):TweetsRecord=
        TweetsRecord(
                id = tweetsId,
                createdAt = now,
                modifiedAt = now,
                deletedAt = Instant.EPOCH,
                version = 0,
                message = "message",
                comment = "comment",
                status = TweetStatus.DRAFT
        )
}


fun TweetsRecord.randomized(preserveIds: Boolean): TweetsRecord {
    val instantMin: Instant = Instant.EPOCH
    val instantMax: Instant = (Instant.now() + Duration.ofDays(50 * 365))
    return TweetsRecord(
            id = when (preserveIds) {
                true -> id
                false -> UUID.randomUUID()
            },
            createdAt = (instantMin..instantMax).random(),
            modifiedAt = (instantMin..instantMax).random(),
            deletedAt = (instantMin..instantMax).random(),
            version = (0..1000).random(),
            message = randomString(prefix = "msg-"),
            comment = randomString(prefix = "comment-"),
            status = randomEnumValue()
    )
}
