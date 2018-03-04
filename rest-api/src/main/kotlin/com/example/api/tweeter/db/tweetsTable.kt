package com.example.api.tweeter.db

import com.example.util.exposed.instant
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.time.Instant
import java.util.*

object TweetsTable : Table("tweet") {
    val id = uuid("id").primaryKey()
    val createdAt = instant("created_at")
    val modifiedAt = instant("updated_at")
    val version = integer("version")
    val message = text("message")
    val comment = text("comment").nullable()
}

data class TweetsRecord(
        val id: UUID, val createdAt: Instant, val modifiedAt: Instant, val version: Int,
        val message: String, val comment: String?
)

fun TweetsTable.rowToTweetsRecord(row: ResultRow): TweetsRecord =
        TweetsRecord(
                id = row[id],
                createdAt = row[createdAt],
                modifiedAt = row[modifiedAt],
                version = row[version],
                message = row[message],
                comment = row[comment]
        )
