package com.example.api.bookstore.db

import com.example.api.common.rest.error.exception.EntityNotFoundException
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional // Should be at @Service level in real applications
class AuthorRepository {
    private val crudTable = AuthorTable

    fun insert(authorRecord: AuthorRecord): AuthorRecord = crudTable
            .insert {
                it[id] = authorRecord.id
                it[createdAt] = authorRecord.createdAt
                it[modifiedAt] = authorRecord.modifiedAt
                it[version] = authorRecord.version
                it[name] = authorRecord.name
            }.let { this[authorRecord.id] }

    fun update(authorRecord: AuthorRecord): AuthorRecord = updatePartial(authorRecord = authorRecord) { cols: ColumnList ->
        cols - listOf(crudTable.id)
    }

    fun updatePartial(authorRecord: AuthorRecord, columnsToUpdate: (ColumnList) -> ColumnList): AuthorRecord {
        val recordId = authorRecord.id
        val fieldsToUpdate: ColumnList = columnsToUpdate(crudTable.columns)
        if (fieldsToUpdate.isEmpty()) {
            return this[recordId]
        }

        return crudTable.update({ crudTable.id eq recordId }) { stmt ->
            listOf(
                    Pair(createdAt, { stmt[createdAt] = authorRecord.createdAt }),
                    Pair(modifiedAt, { stmt[modifiedAt] = authorRecord.modifiedAt }),
                    Pair(version, { stmt[version] = authorRecord.version }),
                    Pair(name, { stmt[name] = authorRecord.name })
            )
                    .filter { p -> p.first in fieldsToUpdate }
                    .forEach { p -> p.second() }
        }.let { this[recordId] }
    }

    fun findAll(): List<AuthorRecord> = crudTable.selectAll().map { it.toAuthorRecord() }
    fun requireIdExists(id: UUID): UUID = this[id].id

    operator fun get(id: UUID): AuthorRecord = findOneById(id)
            ?: throw EntityNotFoundException("AuthorRecord NOT FOUND ! (id=$id)")

    fun findOneById(id: UUID): AuthorRecord? = crudTable
            .select { AuthorTable.id eq id }
            .limit(1)
            .map { it.toAuthorRecord() }
            .firstOrNull()

}

private fun ResultRow.toAuthorRecord() = AuthorTable.rowToAuthorRecord(this)
