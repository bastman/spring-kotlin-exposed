package com.example.api.bookstore.domain.repo

import com.example.api.bookstore.domain.db.BookRecord
import com.example.api.bookstore.domain.db.BookTable
import com.example.api.bookstore.domain.db.toBookRecord
import com.example.api.common.EntityNotFoundException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional // Should be at @Service level in real applications
class BookRepository {

    fun insert(book: BookRecord): BookRecord {
        BookTable.insert({
            it[id] = book.id
            it[authorId] = book.authorId
            it[createdAt] = book.createdAt
            it[modifiedAt] = book.modifiedAt
            it[version] = book.version
            it[title] = book.title
            it[status] = book.status
            it[price] = book.price
        })

        return requireOneById(book.id)
    }

    fun update(book: BookRecord): BookRecord {
        BookTable.update({ BookTable.id eq book.id }) {
            it[createdAt] = book.createdAt
            it[modifiedAt] = book.modifiedAt
            it[version] = book.version
            it[title] = book.title
            it[status] = book.status
            it[price] = book.price
        }

        return requireOneById(book.id)
    }

    fun requireOneById(id: UUID): BookRecord
            = getOneById(id) ?: throw EntityNotFoundException("BookRecord NOT FOUND ! (id=$id)")

    fun getOneById(id: UUID): BookRecord? =
            BookTable.select { BookTable.id eq id }
                    .limit(1)
                    .map { it.toBookRecord() }
                    .firstOrNull()

    fun findAll() = BookTable.selectAll().map { it.toBookRecord() }
}