package com.example.api.bookstore.db

import com.example.api.common.EntityNotFoundException
import org.jetbrains.exposed.sql.*
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

        return this[book.id]
    }

    fun update(book: BookRecord): BookRecord {
        BookTable.update({ BookTable.id eq book.id }) {
            it[modifiedAt] = book.modifiedAt
            it[version] = book.version
            it[title] = book.title
            it[status] = book.status
            it[price] = book.price
        }

        return this[book.id]
    }

    operator fun get(id: UUID): BookRecord = findOneById(id)
            ?: throw EntityNotFoundException("BookRecord NOT FOUND ! (id=$id)")

    fun findOneById(id: UUID): BookRecord? =
            BookTable.select { BookTable.id eq id }
                    .limit(1)
                    .map { it.toBookRecord() }
                    .firstOrNull()

    fun findByIdList(ids: List<UUID>): List<BookRecord> =
            BookTable.select { BookTable.id inList ids.distinct() }
                    .map { it.toBookRecord() }

    fun findAll(): List<BookRecord> = BookTable.selectAll().map { it.toBookRecord() }

    fun findAllByAuthorIdList(ids: List<UUID>): List<BookRecord> =
            BookTable.select { BookTable.authorId inList ids.distinct() }
                    .map { it.toBookRecord() }

    fun findAllBooksJoinAuthor() =
            (AuthorTable innerJoin BookTable)
                    .selectAll()
                    .map {
                        BookRecordJoinAuthorRecord(
                                bookRecord = it.toBookRecord(),
                                authorRecord = it.toAuthorRecord()
                        )
                    }

    fun findOneJoinAuthor(id: UUID) =
            (AuthorTable innerJoin BookTable)
                    .select { BookTable.id eq id }
                    .limit(1)
                    .map {
                        BookRecordJoinAuthorRecord(
                                bookRecord = it.toBookRecord(),
                                authorRecord = it.toAuthorRecord()
                        )
                    }
                    .firstOrNull()

    fun requireOneJoinAuthor(id: UUID): BookRecordJoinAuthorRecord =
            findOneJoinAuthor(id) ?: throw EntityNotFoundException("BookRecord NOT FOUND ! (id=$id)")


}

data class BookRecordJoinAuthorRecord(val bookRecord: BookRecord, val authorRecord: AuthorRecord)

private fun ResultRow.toBookRecord() = BookTable.rowToBookRecord(this)
private fun ResultRow.toAuthorRecord() = AuthorTable.rowToAuthorRecord(this)