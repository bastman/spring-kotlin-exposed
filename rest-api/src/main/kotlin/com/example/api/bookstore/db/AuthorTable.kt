package com.example.api.bookstore.db

import com.example.util.exposed.columnTypes.instant
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.time.Instant
import java.util.*

object AuthorTable : Table("author") {
    val id = uuid("id").primaryKey()
    val createdAt = instant("created_at")
    val modifiedAt = instant("updated_at")
    val version = integer("version")
    val name = text("name")
}

data class AuthorRecord(
        val id: UUID, val createdAt: Instant, val modifiedAt: Instant, val version: Int,
        val name: String
)

fun AuthorTable.rowToAuthorRecord(row: ResultRow): AuthorRecord =
        AuthorRecord(
                id = row[id],
                createdAt = row[createdAt],
                modifiedAt = row[modifiedAt],
                version = row[version],
                name = row[name]
        )
