package com.example.api.bookstore.db

import com.example.api.bookstore.fixtures.BookstoreApiFixtures
import com.example.api.bookstore.fixtures.randomized
import com.example.api.common.rest.error.exception.EntityNotFoundException
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

class AuthorRepoTest(
        @Autowired private val repo: AuthorRepository
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
        val recordNew = BookstoreApiFixtures.newAuthorRecord(authorId = id, now = now)
        val recordInserted = repo.insert(recordNew)
        recordInserted shouldEqualRecursively recordNew

        run {
            val recordSource: AuthorRecord = repo[id]
            val recordToBeModified: AuthorRecord = recordSource
                    .copy(version = 1, name = "other-name", modifiedAt = Instant.now())
            val recordUpdated: AuthorRecord = repo.update(recordToBeModified)

            recordUpdated shouldEqualRecursively recordToBeModified
        }

        val recordFromDb: AuthorRecord? = repo
                .findAll()
                .firstOrNull { it.id == id }

        recordFromDb shouldBeInstanceOf AuthorRecord::class
        recordFromDb!!
        recordFromDb.id shouldEqual recordNew.id
    }

    @TestFactory
    fun `some random crud ops should work`() = minuTestFactory {
        val testCases: List<TestCase> = (0..100).map {
            val recordId = UUID.randomUUID()
            val recordNew = BookstoreApiFixtures.newAuthorRecord(authorId = recordId, now = Instant.now())
                    .randomized(preserveIds = true)
            TestCase(
                    recordNew = recordNew,
                    recordsUpdate = (0..10).map {
                        recordNew.randomized(preserveIds = true)
                    }
            )
        }

        testCases.forEach { testCase ->
            context("test: : ${testCase.recordNew}") {
                test("INSERT: ${testCase.recordNew}") {
                    val inserted: AuthorRecord = repo.insert(testCase.recordNew)
                    inserted shouldEqualRecursively testCase.recordNew
                }
                test("GET: ${testCase.recordNew}") {
                    val loaded: AuthorRecord = repo.get(id = testCase.recordNew.id)
                    loaded shouldEqualRecursively testCase.recordNew
                }
                testCase.recordsUpdate.forEachIndexed { index, recordToUpdate ->
                    test("UPDATE ($index): $recordToUpdate") {
                        val updated: AuthorRecord = repo.update(recordToUpdate)
                        updated shouldEqualRecursively recordToUpdate
                    }
                }
            }
        }
    }

    private data class TestCase(
            val recordNew: AuthorRecord,
            val recordsUpdate: List<AuthorRecord>
    )

}
