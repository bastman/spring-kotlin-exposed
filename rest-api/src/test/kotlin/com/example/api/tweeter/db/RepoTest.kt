package com.example.api.tweeter.db

import com.example.api.common.rest.error.exception.EntityNotFoundException
import com.example.testutils.assertions.shouldEqualRecursively
import com.example.testutils.minutest.minuTestFactory
import com.example.testutils.random.random
import com.example.testutils.random.randomEnumValue
import com.example.testutils.random.randomString
import com.example.testutils.spring.BootWebMockMvcTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotEqual
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
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

    @TestFactory
    fun `some random crud ops should work`() = minuTestFactory {
        val testCases: List<TestCase> = (0..100).map {
            val instantMin: Instant = Instant.EPOCH
            val instantMax: Instant = (Instant.now() + Duration.ofDays(50 * 365))
            TestCase(
                    recordNew = TweetsRecord(
                            id = UUID.randomUUID(),
                            createdAt = (instantMin..instantMax).random(),
                            modifiedAt = (instantMin..instantMax).random(),
                            deletedAt = (instantMin..instantMax).random(),
                            version = (0..1000).random(),
                            message = randomString(prefix = "msg-"),
                            comment = randomString(prefix = "comment-"),
                            status = randomEnumValue()
                    )
            )
        }

        testCases.forEach { testCase ->
            test("test INSERT: ${testCase.recordNew}") {
                val inserted: TweetsRecord = repo.insert(testCase.recordNew)
                inserted shouldEqualRecursively testCase.recordNew
            }
        }
    }


    private data class TestCase(
            val recordNew: TweetsRecord
    )

}
