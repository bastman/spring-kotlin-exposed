package com.example.api.tweeter.db

import com.example.api.common.EntityNotFoundException
import com.example.api.tweeter.db.TweetsTable.id
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional // Should be at @Service level in real applications
class TweetsRepo {

    fun insert(record: TweetsRecord): TweetsRecord {
        try {
            TweetsTable.insert {
                it[id] = record.id
                it[createdAt] = record.createdAt
                it[modifiedAt] = record.modifiedAt
                it[deletedAt] = record.deletedAt
                it[version] = record.version
                it[message] = record.message
                it[comment] = record.comment
                it[status] = record.status
            }
        } catch (all: Throwable) {
            all.printStackTrace()

            throw all
        }
        return record
    }

    fun update(record: TweetsRecord): TweetsRecord {
        TweetsTable.update({ id eq record.id }) {
            it[createdAt] = record.createdAt
            it[modifiedAt] = record.modifiedAt
            it[version] = record.version
            it[message] = record.message
            it[comment] = record.comment
        }
        return record
    }

    fun findAll() = TweetsTable.selectAll().map { it.toTweetsRecord() }

    fun findOneById(id: UUID): TweetsRecord? =
            TweetsTable.select { TweetsTable.id eq id }
                    .limit(1)
                    .map { it.toTweetsRecord() }
                    .firstOrNull()

    operator fun get(id: UUID): TweetsRecord = findOneById(id)
            ?: throw EntityNotFoundException("TweetRecord NOT FOUND ! (id=$id)")

}

private fun ResultRow.toTweetsRecord() = TweetsTable.rowToTweetsRecord(this)


