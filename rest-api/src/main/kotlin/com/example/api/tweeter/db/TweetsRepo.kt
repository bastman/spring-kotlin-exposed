package com.example.api.tweeter.db

import com.example.api.common.rest.error.exception.EntityNotFoundException
import com.example.api.tweeter.db.TweetsTable.id
import mu.KLogging
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional // Should be at @Service level in real applications
class TweetsRepo {
    companion object : KLogging()

    private val crudTable = TweetsTable

    fun insert(record: TweetsRecord): TweetsRecord = crudTable
            .insert {
                it[id] = record.id
                it[createdAt] = record.createdAt
                it[modifiedAt] = record.modifiedAt
                it[deletedAt] = record.deletedAt
                it[version] = record.version
                it[message] = record.message
                it[comment] = record.comment
                it[status] = record.status
            }
            .let { this[record.id] }
            .also { logger.info { "INSERT: table: ${crudTable.tableName} id: ${record.id} record: $it" } }


    fun update(record: TweetsRecord): TweetsRecord = crudTable
            .update({ id eq record.id }) {
                it[createdAt] = record.createdAt
                it[modifiedAt] = record.modifiedAt
                it[deletedAt] = record.deletedAt
                it[version] = record.version
                it[message] = record.message
                it[comment] = record.comment
                it[status] = record.status
            }
            .let { this[record.id] }
            .also { logger.info { "UPDATE: table: ${crudTable.tableName} id: ${record.id} record: $it" } }

    fun findAll() = TweetsTable
            .selectAll()
            .map { with(TweetsTable) { it.toTweetsRecord() } }

    fun findOneById(id: UUID): TweetsRecord? =
            TweetsTable.select { TweetsTable.id eq id }
                    .limit(1)
                    .map { with(TweetsTable) { it.toTweetsRecord() } }
                    .firstOrNull()

    operator fun get(id: UUID): TweetsRecord = findOneById(id)
            ?: throw EntityNotFoundException("TweetRecord NOT FOUND ! (id=$id)")

}




