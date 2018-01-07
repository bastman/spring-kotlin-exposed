package com.example.api.bookstore.domain.db

import com.example.util.exposed.instant
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
        val id: UUID,
        val createdAt: Instant,
        val modifiedAt: Instant,
        val version: Int,
        val name: String
)

fun ResultRow.toAuthorRecord() =
        AuthorRecord(
                id = this[AuthorTable.id],
                createdAt = this[AuthorTable.createdAt],
                modifiedAt = this[AuthorTable.modifiedAt],
                version = this[AuthorTable.version],
                name = this[AuthorTable.name]
        )