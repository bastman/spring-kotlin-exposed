package com.example.api.bookstore

import com.example.api.bookstore.db.AuthorRecord
import com.example.api.bookstore.db.BookRecord
import com.example.api.bookstore.db.BookRecordJoinAuthorRecord
import com.example.api.bookstore.db.BookStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class AuthorCreateRequest(val name: String)
data class AuthorUpdateRequest(val name: String)
data class BookCreateRequest(val authorId: UUID, val title: String, val status: BookStatus, val price: BigDecimal)
data class BookUpdateRequest(val title: String, val status: BookStatus, val price: BigDecimal)

data class AuthorDto(val id: UUID, val createdAt: Instant, val modifiedAt: Instant, val name: String)
data class AuthorWithBooksDto(val author: AuthorDto, val books: List<BookDto>)

data class BookDto(
        val id: UUID, val createdAt: Instant, val modifiedAt: Instant,
        val title: String, val status: BookStatus, val price: BigDecimal,
        val authorId: UUID
)

data class BookWithAuthorDto(
        val id: UUID, val createdAt: Instant, val modifiedAt: Instant,
        val title: String, val status: BookStatus, val price: BigDecimal,
        val author: AuthorDto
)

fun AuthorCreateRequest.toAuthorRecord(id: UUID, now: Instant): AuthorRecord =
        AuthorRecord(id = id, version = 0, createdAt = now, modifiedAt = now, name = name)

fun BookCreateRequest.toBookRecord(id: UUID, now: Instant): BookRecord =
        BookRecord(
                id = id, version = 0, createdAt = now, modifiedAt = now,
                authorId = authorId,
                title = title, status = status, price = price
        )

fun AuthorRecord.toAuthorDto() =
        AuthorDto(id = id, createdAt = createdAt, modifiedAt = modifiedAt, name = name)

fun BookRecord.toBookDto() =
        BookDto(
                id = id, createdAt = createdAt, modifiedAt = modifiedAt,
                authorId = authorId,
                title = title, status = status, price = price
        )

fun AuthorRecord.toAuthorWithBooksDto(books: List<BookRecord>) =
        AuthorWithBooksDto(author = this.toAuthorDto(), books = books.map { it.toBookDto() })

fun BookRecordJoinAuthorRecord.toBookWithAuthorDto() =
        BookWithAuthorDto(
                id = bookRecord.id,
                createdAt = bookRecord.createdAt,
                modifiedAt = bookRecord.modifiedAt,
                title = bookRecord.title,
                status = bookRecord.status,
                price = bookRecord.price,
                author = authorRecord.toAuthorDto()
        )