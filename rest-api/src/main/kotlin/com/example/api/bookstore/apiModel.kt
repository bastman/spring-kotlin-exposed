package com.example.api.bookstore

import com.example.api.bookstore.domain.db.AuthorRecord
import com.example.api.bookstore.domain.db.BookRecord
import com.example.api.bookstore.domain.db.BookStatus
import com.example.api.bookstore.domain.repo.BookRecordJoinAuthorRecord
import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class AuthorCreateRequest(val name: String)
data class AuthorUpdateRequest(val name: String)

fun AuthorCreateRequest.toRecord(): AuthorRecord {
    val now = Instant.now()
    return AuthorRecord(
            id = UUID.randomUUID(),
            version = 0,
            createdAt = now,
            modifiedAt = now,
            name = name
    )
}

data class BookCreateRequest(val authorId: UUID, val title: String, val status: BookStatus, val price: BigDecimal)
data class BookUpdateRequest(val title: String, val status: BookStatus, val price: BigDecimal)

fun BookCreateRequest.toRecord(): BookRecord {
    val now = Instant.now()
    return BookRecord(
            id = UUID.randomUUID(),
            version = 0,
            createdAt = now,
            modifiedAt = now,
            authorId = authorId,
            title = title,
            status = status,
            price = price
    )
}


data class BookDto(
        val id: UUID,
        val createdAt: Instant,
        val modifiedAt: Instant,
        val title: String,
        val status: BookStatus,
        val price: BigDecimal,
        val author: AuthorDto
)

data class AuthorDto(
        val id: UUID, val createdAt: Instant, val modifiedAt: Instant, val name: String
)

private fun AuthorRecord.toAuthorDto() =
        AuthorDto(id = id, createdAt = createdAt, modifiedAt = modifiedAt, name = name)


fun BookRecordJoinAuthorRecord.toBookDto() =
        BookDto(
                id = bookRecord.id,
                createdAt = bookRecord.createdAt,
                modifiedAt = bookRecord.modifiedAt,
                title = bookRecord.title,
                status = bookRecord.status,
                price = bookRecord.price,
                author = authorRecord.toAuthorDto()
        )