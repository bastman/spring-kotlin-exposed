package com.example.api.tweeter.db

import com.example.api.common.rest.error.exception.EntityNotFoundException
import com.example.testutils.spring.BootWebMockMvcTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotEqual
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*

class TweetsRepoTest(
        @Autowired private val repo: TweetsRepo
) : BootWebMockMvcTest() {

    @Test
    fun `unhappy - handle unknown id's `() {
        val unknownId: UUID = UUID.randomUUID()

        repo.findOneById(id = unknownId) shouldBe null

        assertThrows<EntityNotFoundException> { repo.get(id = unknownId) }
        assertThrows<EntityNotFoundException> { repo[unknownId] }
    }

    @Test
    fun `basic crud ops should work`() {
        val id: UUID = UUID.randomUUID()
        val now: Instant = Instant.now()
        val recordNew = TweetsRecord(
                id = id,
                createdAt = now,
                modifiedAt = now,
                deletedAt = Instant.EPOCH,
                version = 0,
                message = "message-$id",
                comment = "comment-$id",
                status = TweetStatus.DRAFT
        )
        val recordInserted = repo.insert(recordNew)
        recordInserted shouldEqual recordNew

        TweetStatus.values().forEach { statusToBeApplied: TweetStatus ->
            val recordSource: TweetsRecord = repo[id]
            val recordToBeModified: TweetsRecord = recordSource
                    .copy(status = statusToBeApplied, modifiedAt = Instant.now())
            val recordUpdated: TweetsRecord = repo.update(recordToBeModified)

            recordUpdated shouldEqual recordToBeModified
            recordUpdated.status shouldEqual statusToBeApplied
            recordUpdated.modifiedAt shouldNotEqual recordSource.modifiedAt
        }

        val recordFromDb: TweetsRecord? = repo
                .findAll()
                .firstOrNull { it.id == id }

        recordFromDb shouldBeInstanceOf TweetsRecord::class
        recordFromDb!!
        recordFromDb.id shouldEqual recordNew.id
        recordFromDb.message shouldEqual recordNew.message
        recordFromDb.comment shouldEqual recordNew.comment
    }


}