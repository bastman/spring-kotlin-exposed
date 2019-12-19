package com.example.api.bookstore.db

import com.example.api.common.rest.error.exception.EntityNotFoundException
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional // Should be at @Service level in real applications
class BookRepository {
    val crudTable = BookTable

    fun insert(bookRecord: BookRecord): BookRecord = crudTable
            .insert {
                it[id] = bookRecord.id
                it[authorId] = bookRecord.authorId
                it[createdAt] = bookRecord.createdAt
                it[modifiedAt] = bookRecord.modifiedAt
                it[version] = bookRecord.version
                it[title] = bookRecord.title
                it[status] = bookRecord.status
                it[price] = bookRecord.price
            }.let { this[bookRecord.id] }

    fun update(bookRecord: BookRecord): BookRecord = updatePartial(bookRecord = bookRecord) { cols: ColumnList ->
        cols.filter { it != crudTable.id }
    }

    fun updatePartial(bookRecord: BookRecord, columnsToUpdate: (ColumnList) -> ColumnList): BookRecord {
        val recordId = bookRecord.id
        val fieldsToUpdate: ColumnList = columnsToUpdate(crudTable.columns)
        if (fieldsToUpdate.isEmpty()) {
            return this[recordId]
        }

        return crudTable.update({ crudTable.id eq recordId }) { stmt ->
            listOf(
                    Pair(id, { stmt[id] = bookRecord.id }),
                    Pair(createdAt, { stmt[createdAt] = bookRecord.createdAt }),
                    Pair(modifiedAt, { stmt[modifiedAt] = bookRecord.modifiedAt }),
                    Pair(version, { stmt[version] = bookRecord.version }),
                    Pair(title, { stmt[title] = bookRecord.title }),
                    Pair(status, { stmt[status] = bookRecord.status }),
                    Pair(price, { stmt[price] = bookRecord.price })
            )
                    .filter { p -> p.first in fieldsToUpdate }
                    .forEach { p -> p.second() }
        }.let { this[recordId] }
    }


    operator fun get(id: UUID): BookRecord = findOneById(id)
            ?: throw EntityNotFoundException("BookRecord NOT FOUND ! (id=$id)")

    fun findOneById(id: UUID): BookRecord? = crudTable
            .select { crudTable.id eq id }
            .limit(1)
            .map { it.toBookRecord() }
            .firstOrNull()

    fun findByIdList(ids: List<UUID>): List<BookRecord> = crudTable
            .select { BookTable.id inList ids.distinct() }
            .map { it.toBookRecord() }

    fun findAll(): List<BookRecord> = crudTable.selectAll().map { it.toBookRecord() }

    fun findAllByAuthorIdList(ids: List<UUID>): List<BookRecord> = crudTable
            .select { crudTable.authorId inList ids.distinct() }
            .map { it.toBookRecord() }

    fun findAllBooksJoinAuthor() =
            (AuthorTable innerJoin crudTable)
                    .selectAll()
                    .map {
                        BookRecordJoinAuthorRecord(
                                bookRecord = it.toBookRecord(),
                                authorRecord = it.toAuthorRecord()
                        )
                    }

    fun findOneJoinAuthor(id: UUID) =
            (AuthorTable innerJoin crudTable)
                    .select { crudTable.id eq id }
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
