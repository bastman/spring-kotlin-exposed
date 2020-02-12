package com.example.api.tweeter.db

import com.example.util.exposed.columnTypes.enumerationBySqlType
import com.example.util.exposed.columnTypes.instant
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.time.Instant
import java.util.*

object TweetsTable : Table("tweet") {
    val id = uuid("id").primaryKey()
    val createdAt = instant("created_at")
    val modifiedAt = instant("updated_at")
    val deletedAt = instant("deleted_at").default(Instant.EPOCH)
    val version = integer("version")
    val message = varchar("message", 255)
    val comment = text("comment").nullable()
    val status = enumerationBySqlType(
            name = "status", sqlType = "TweetStatusType", klass = TweetStatus::class.java,
            serialize = { it.dbValue },
            unserialize = { fromDb, toKlass ->
                toKlass.enumConstants.first { it.dbValue == fromDb }
            }
    ).default(TweetStatus.DRAFT)

    fun ResultRow.toTweetsRecord() = TweetsTable.rowToTweetsRecord(this)
}

enum class TweetStatus(val dbValue: String) { DRAFT("DRAFT"), PENDING("PENDING"), PUBLISHED("PUBLISHED"); }

data class TweetsRecord(
        val id: UUID, val createdAt: Instant, val modifiedAt: Instant, val deletedAt: Instant, val version: Int,
        val message: String, val comment: String?, val status: TweetStatus
)

fun TweetsTable.rowToTweetsRecord(row: ResultRow): TweetsRecord =
        TweetsRecord(
                id = row[id],
                createdAt = row[createdAt],
                modifiedAt = row[modifiedAt],
                deletedAt = row[deletedAt],
                version = row[version],
                message = row[message],
                comment = row[comment],
                status = row[status]
        )
