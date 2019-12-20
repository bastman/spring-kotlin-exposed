package com.example.api.tweeter.db

import com.example.api.common.rest.error.exception.EntityNotFoundException
import com.example.api.tweeter.fixtures.TweeterApiFixtures
import com.example.api.tweeter.fixtures.randomized
import com.example.testutils.assertions.shouldEqualRecursively
import com.example.testutils.minutest.minuTestFactory
import com.example.testutils.spring.BootWebMockMvcTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
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
        val recordNew: TweetsRecord = TweeterApiFixtures.newTweetsRecord()
        val recordId: UUID = recordNew.id

        repo.insert(recordNew)
                .also { it shouldEqualRecursively recordNew }

        TweetStatus.values().forEach { statusToBeApplied: TweetStatus ->
            val recordSource: TweetsRecord = repo[recordId]
            val recordToBeModified: TweetsRecord = recordSource
                    .copy(status = statusToBeApplied, modifiedAt = Instant.now())
            repo.update(recordToBeModified)
                    .also { it shouldEqualRecursively recordToBeModified }
            repo[recordId]
                    .also { it shouldEqualRecursively recordToBeModified }
        }

        val recordFromDb: TweetsRecord? = repo
                .findAll()
                .firstOrNull { it.id == recordId }

        recordFromDb shouldBeInstanceOf TweetsRecord::class
        recordFromDb!!
        recordFromDb.id shouldEqual recordNew.id
        recordFromDb.message shouldEqual recordNew.message
        recordFromDb.comment shouldEqual recordNew.comment
    }

    @TestFactory
    fun `some random crud ops should work`() = minuTestFactory {
        val testCases: List<TestCase> = (0..100).map {
            val recordNew: TweetsRecord = TweeterApiFixtures
                    .newTweetsRecord()
                    .randomized(preserveIds = true)

            TestCase(
                    recordNew = recordNew,
                    recordsUpdate = (0..10).map {
                        recordNew.randomized(preserveIds = true)
                    }
            )
        }
        context("prepare ...") {
            testCases.onEach { testCase ->
                context("prepare: ${testCase.recordNew}") {
                    testCase.recordNew shouldEqualRecursively testCase.recordNew
                    testCase shouldEqualRecursively testCase
                }
            }
        }

        testCases.forEachIndexed { testCaseIndex, testCase ->
            context("test ($testCaseIndex): ${testCase.recordNew}") {

                context("DB INSERT") {
                    test("INSERT: ${testCase.recordNew}") {
                        repo.insert(testCase.recordNew)
                                .also { it shouldEqualRecursively testCase.recordNew }
                    }
                    test("GET INSERTED: ${testCase.recordNew}") {
                        repo.get(id = testCase.recordNew.id)
                                .also { it shouldEqualRecursively testCase.recordNew }
                    }
                }
                context("DB UPDATE") {
                    testCase.recordsUpdate.forEachIndexed { index, recordToUpdate ->
                        context("DB UPDATE ($index): $recordToUpdate") {
                            test("UPDATE ($index): $recordToUpdate") {
                                repo.update(recordToUpdate)
                                        .also { it shouldEqualRecursively recordToUpdate }
                            }
                            test("GET UPDATED ($index): $recordToUpdate") {
                                repo.get(id = recordToUpdate.id)
                                        .also { it shouldEqualRecursively recordToUpdate }
                            }
                        }
                    }
                }

            }

        }
    }

    private data class TestCase(
            val recordNew: TweetsRecord,
            val recordsUpdate: List<TweetsRecord>
    )

}
