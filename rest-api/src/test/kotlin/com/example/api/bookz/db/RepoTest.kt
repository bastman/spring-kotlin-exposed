package com.example.api.bookz.db

import com.example.api.bookz.fixtures.BookzApiFixtures
import com.example.api.bookz.fixtures.randomized
import com.example.api.common.rest.error.exception.EntityNotFoundException
import com.example.testutils.assertions.shouldEqualRecursively
import com.example.testutils.minutest.minuTestFactory
import com.example.testutils.spring.BootWebMockMvcTest
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*

class BookzRepoTest(
        @Autowired private val repo: BookzRepo
) : BootWebMockMvcTest() {

    @Test
    fun `unhappy - handle unknown id's `() {
        val unknownId: UUID = UUID.randomUUID()

        repo.findOne(id = unknownId) shouldBe null

        assertThrows<EntityNotFoundException> { repo.get(id = unknownId) }
        assertThrows<EntityNotFoundException> { repo[unknownId] }
    }

    @Test
    fun `basic crud ops should work`() {
        val recordNew: BookzRecord = BookzApiFixtures.newBookzRecord()
        val recordId: UUID = recordNew.id

        repo.insert(recordNew)
                .also { it shouldEqualRecursively recordNew }
        repo.get(id = recordId)
                .also { it shouldEqualRecursively recordNew }

        repo.findAll()
                .firstOrNull { it.id == recordId }
                .also { it shouldEqualRecursively recordNew }

        repo.findAllActive()
                .firstOrNull { it.id == recordId }
                .also { it shouldEqualRecursively recordNew }

        val toUpdate: BookzRecord = recordNew.copy(isActive = false, modifiedAt = Instant.now())
        val recordUpdated: BookzRecord = repo.updateOne(id = recordId) {
            it[modifiedAt] = toUpdate.modifiedAt
            it[isActive] = toUpdate.isActive
        }
                .also { it shouldEqualRecursively toUpdate }
                .also { repo[recordId] shouldEqualRecursively it }

        repo.get(id = recordId)
                .also { it shouldEqualRecursively recordUpdated }
        repo.findAll()
                .firstOrNull { it.id == recordId }
                .also { it shouldEqualRecursively recordUpdated }
        repo.findAllActive()
                .firstOrNull { it.id == recordId }
                .also { it shouldBe null }
    }


    @TestFactory
    fun `some random crud ops should work`() = minuTestFactory {
        val testCases: List<TestCase> = (0..100).map {
            val recordNew: BookzRecord = BookzApiFixtures
                    .newBookzRecord()
                    .randomized(preserveIds = true, preserveIsActive = true)

            TestCase(
                    recordNew = recordNew,
                    recordsUpdate = (0..10).map {
                        recordNew.randomized(preserveIds = true, preserveIsActive = true)
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
            val recordNew: BookzRecord,
            val recordsUpdate: List<BookzRecord>
    )

}
