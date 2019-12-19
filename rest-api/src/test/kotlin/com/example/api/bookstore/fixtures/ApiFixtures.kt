package com.example.api.bookstore.fixtures

import com.example.api.bookstore.db.AuthorRecord
import com.example.api.bookstore.db.BookRecord
import com.example.api.bookstore.db.BookStatus
import com.example.testutils.random.random
import com.example.testutils.random.randomBigDecimal
import com.example.testutils.random.randomEnumValue
import com.example.testutils.random.randomString
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.*

data class BookEntity(val bookRecord: BookRecord, val authorRecord: AuthorRecord)

object BookstoreApiFixtures {
    fun newAuthorRecord(authorId: UUID = UUID.randomUUID(), now: Instant = Instant.now()): AuthorRecord =
            AuthorRecord(
                    id = authorId, createdAt = now, modifiedAt = now, version = 0, name = "name"
            )

    fun newBookRecord(
            bookId: UUID = UUID.randomUUID(), authorId: UUID = UUID.randomUUID(), now: Instant = Instant.now()
    ): BookRecord =
            BookRecord(
                    id = bookId, createdAt = now, modifiedAt = now, version = 0,
                    authorId = authorId, title = "title",
                    status = BookStatus.NEW, price = BigDecimal("100.01")
            )

    fun newBookEntity(
            bookId: UUID = UUID.randomUUID(), authorId: UUID = UUID.randomUUID(), now: Instant = Instant.now()
    ): BookEntity =
            BookEntity(
                    authorRecord = newAuthorRecord(authorId = authorId, now = now),
                    bookRecord = newBookRecord(
                            bookId = bookId, authorId = authorId, now = now
                    )
            )
}

fun AuthorRecord.randomized(preserveIds: Boolean): AuthorRecord {
    val instantMin: Instant = Instant.EPOCH
    val instantMax: Instant = (Instant.now() + Duration.ofDays(50 * 365))
    return AuthorRecord(
            id = when (preserveIds) {
                true -> id
                false -> UUID.randomUUID()
            },
            createdAt = (instantMin..instantMax).random(),
            modifiedAt = (instantMin..instantMax).random(),
            version = (0..1000).random(),
            name = randomString(prefix = "name-")
    )
}

fun BookRecord.randomized(preserveIds: Boolean): BookRecord {
    val instantMin: Instant = Instant.EPOCH
    val instantMax: Instant = (Instant.now() + Duration.ofDays(50 * 365))
    return BookRecord(
            id = when (preserveIds) {
                true -> id
                false -> UUID.randomUUID()
            },
            authorId = when (preserveIds) {
                true -> authorId
                false -> UUID.randomUUID()
            },
            createdAt = (instantMin..instantMax).random(),
            modifiedAt = (instantMin..instantMax).random(),
            version = (0..1000).random(),
            title = randomString(prefix = "title-"),
            status = randomEnumValue(),
            price = (10_000.01..20_000.01).randomBigDecimal()
    )
}
