package com.example.api.bookz.db

import com.example.api.common.EntityNotFoundException
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional // Should be at @Service level in real applications
class BookzRepo {

    fun insert(record: BookzRecord): BookzRecord {
        BookzTable.insert({
            it[id] = record.id
            it[createdAt] = record.createdAt
            it[modifiedAt] = record.modifiedAt
            it[data] = record.data
        })
        return requireOneById(record.id)
    }

    fun update(record: BookzRecord): BookzRecord {
        BookzTable.update({ BookzTable.id eq record.id }) {
            it[createdAt] = record.createdAt
            it[modifiedAt] = record.modifiedAt
            it[data] = record.data
        }

        return requireOneById(record.id)
    }

    fun requireOneById(id: UUID): BookzRecord = getOneById(id)
            ?: throw EntityNotFoundException("BookzRecord NOT FOUND ! (id=$id)")

    fun getOneById(id: UUID): BookzRecord? =
            BookzTable.select { BookzTable.id eq id }
                    .limit(1)
                    .map { it.toBookzRecord() }
                    .firstOrNull()

    fun findAll() =
            BookzTable.selectAll().map { it.toBookzRecord() }


}

private fun ResultRow.toBookzRecord() = BookzTable.rowToBookzRecord(this)

