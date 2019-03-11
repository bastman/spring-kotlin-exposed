package com.example.api.bookstore.db

import com.example.api.common.error.exceptions.EntityNotFoundException
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional // Should be at @Service level in real applications
class AuthorRepository {

    fun insert(record: AuthorRecord): AuthorRecord {
        AuthorTable.insert({
            it[id] = record.id
            it[createdAt] = record.createdAt
            it[modifiedAt] = record.modifiedAt
            it[version] = record.version
            it[name] = record.name
        })

        return this[record.id]
    }

    fun update(record: AuthorRecord): AuthorRecord {
        AuthorTable.update({ AuthorTable.id eq record.id }) {
            it[createdAt] = record.createdAt
            it[modifiedAt] = record.modifiedAt
            it[version] = record.version
            it[name] = record.name
        }

        return this[record.id]
    }

    fun findAll() = AuthorTable.selectAll().map { it.toAuthorRecord() }
    fun requireIdExists(id: UUID): UUID = this[id].id

    operator fun get(id: UUID): AuthorRecord = findOneById(id)
            ?: throw EntityNotFoundException("AuthorRecord NOT FOUND ! (id=$id)")

    fun findOneById(id: UUID): AuthorRecord? =
            AuthorTable.select { AuthorTable.id eq id }
                    .limit(1)
                    .map { it.toAuthorRecord() }
                    .firstOrNull()

}

private fun ResultRow.toAuthorRecord() = AuthorTable.rowToAuthorRecord(this)
