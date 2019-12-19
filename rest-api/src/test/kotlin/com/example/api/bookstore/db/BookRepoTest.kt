package com.example.api.bookstore.db

import com.example.api.bookstore.fixtures.BookEntity
import com.example.api.bookstore.fixtures.BookstoreApiFixtures
import com.example.api.common.rest.error.exception.EntityNotFoundException
import com.example.testutils.assertions.shouldEqualRecursively
import com.example.testutils.spring.BootWebMockMvcTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
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


}
