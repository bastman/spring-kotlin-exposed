package com.example.api.bookstore.db

import com.example.api.bookstore.fixtures.BookEntity
import com.example.api.bookstore.fixtures.BookstoreApiFixtures
import com.example.api.bookstore.fixtures.randomized
import com.example.api.common.rest.error.exception.EntityNotFoundException
import com.example.testutils.assertions.shouldEqualRecursively
import com.example.testutils.minutest.minuTestFactory
import com.example.testutils.spring.BootWebMockMvcTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.jetbrains.exposed.sql.select
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*

class BookRepoTest(
        @Autowired private val authorRepo: AuthorRepository,
        @Autowired private val bookRepo: BookRepository
) : BootWebMockMvcTest() {

    @Test
    fun `unhappy - handle unknown id's `() {
        val repo = bookRepo
        val unknownId: UUID = UUID.randomUUID()

        repo.findOneById(id = unknownId) shouldBe null

        assertThrows<EntityNotFoundException> { repo.get(id = unknownId) }
        assertThrows<EntityNotFoundException> { repo[unknownId] }
    }

    private fun insertBookEntityIntoDB(bookEntity: BookEntity): BookEntity =
            BookEntity(
                    authorRecord = authorRepo.insert(bookEntity.authorRecord),
                    bookRecord = bookRepo.insert(bookEntity.bookRecord)
            )

    private fun getBookEntityFromDb(bookId: UUID): BookEntity {
        return (BookTable innerJoin AuthorTable)
                .select { BookTable.id.eq(bookId) }
                .limit(n = 1, offset = 0)
                .map {
                    BookEntity(
                            authorRecord = AuthorTable.rowToAuthorRecord(it),
                            bookRecord = BookTable.rowToBookRecord(it)
                    )
                }.firstOrNull()
                ?: error("Book Entity Not found! bookid: $bookId")
    }

    @Test
    fun `basic crud ops should work`() {
        val entityNew: BookEntity = BookstoreApiFixtures.newBookEntity()
        val bookId = entityNew.bookRecord.id
        insertBookEntityIntoDB(entityNew)
                .also {
                    it.authorRecord shouldEqualRecursively entityNew.authorRecord
                    it.bookRecord shouldEqualRecursively entityNew.bookRecord
                    it shouldEqualRecursively entityNew
                }

        run {
            val repo = bookRepo
            val recordSource: BookRecord = repo[bookId]
            val recordToBeModified: BookRecord = recordSource
                    .copy(version = 20, title = "other-title",
                            status = BookStatus.PUBLISHED, price = 99.95.toBigDecimal(), modifiedAt = Instant.now())
            val recordUpdated: BookRecord = repo.update(recordToBeModified)

            recordUpdated shouldEqualRecursively recordToBeModified
        }

        run {
            val repo = bookRepo
            val recordFromDb: BookRecord? = repo
                    .findAll()
                    .firstOrNull { it.id == bookId }

            recordFromDb shouldBeInstanceOf BookRecord::class
            recordFromDb!!
            recordFromDb.id shouldEqual entityNew.bookRecord.id
        }
    }


    @TestFactory
    fun `some random crud ops should work`() = minuTestFactory {
        val authorRecordInserted = BookstoreApiFixtures
                .newAuthorRecord()
                .let(authorRepo::insert)
        val authorId: UUID = authorRecordInserted.id
        val testCases: List<TestCase> = (0..100)
                .map {
                    val recordNew = BookstoreApiFixtures.newBookRecord(authorId = authorId)
                            .randomized(preserveIds = true)
                    TestCase(
                            recordNew = recordNew,
                            recordsUpdate = (0..10).map {
                                recordNew.randomized(preserveIds = true)
                            }
                    )
                }

        testCases.onEach { testCase ->
            context("prepare: ${testCase.recordNew}") {
                testCase.recordNew shouldEqualRecursively testCase.recordNew
                testCase shouldEqualRecursively testCase
            }
        }
                //.take(20)
                .forEach { testCase ->

                    context("test: : ${testCase.recordNew}") {
                        test("INSERT: ${testCase.recordNew}") {
                            val recordInserted: BookRecord = bookRepo.insert(testCase.recordNew)
                            recordInserted shouldEqualRecursively testCase.recordNew

                            val entityGiven = getBookEntityFromDb(bookId = recordInserted.id)
                            val entityExpected = BookEntity(
                                    bookRecord = recordInserted, authorRecord = authorRecordInserted
                            )
                            entityGiven shouldEqualRecursively entityExpected
                        }
                        test("GET: ${testCase.recordNew}") {
                            val recordLoaded: BookRecord = bookRepo.get(id = testCase.recordNew.id)
                            recordLoaded shouldEqualRecursively testCase.recordNew

                            val entityGiven = getBookEntityFromDb(bookId = recordLoaded.id)
                            val entityExpected = BookEntity(
                                    bookRecord = recordLoaded, authorRecord = authorRecordInserted
                            )
                            // entityGiven shouldEqualRecursively entityExpected
                        }
                        testCase.recordsUpdate.forEachIndexed { index, recordToUpdate ->
                            test("UPDATE ($index): $recordToUpdate") {
                                val recordUpdated: BookRecord = bookRepo.update(recordToUpdate)
                                recordUpdated shouldEqualRecursively recordToUpdate

                                val entityGiven = getBookEntityFromDb(bookId = recordUpdated.id)
                                val entityExpected = BookEntity(
                                        bookRecord = recordUpdated, authorRecord = authorRecordInserted
                                )
                                // entityGiven shouldEqualRecursively entityExpected
                            }
                        }
                    }
                }
    }

    private data class TestCase(
            val recordNew: BookRecord,
            val recordsUpdate: List<BookRecord>
    )


}
