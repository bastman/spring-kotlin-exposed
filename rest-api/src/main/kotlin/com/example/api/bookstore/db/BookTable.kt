package com.example.api.bookstore.db

import com.example.util.exposed.columnTypes.instant
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.*

object BookTable : Table("book") {
    val id = uuid("id").primaryKey()
    val createdAt = instant("created_at")
    val modifiedAt = instant("updated_at")
    val version = integer("version")
    val authorId = (uuid("author_id") references AuthorTable.id)
    val title = varchar("title", 255)
    val status = enumerationByName("status", 255, BookStatus::class.java)
    val price = decimal("price", 15, 2)
}

data class BookRecord(
        val id: UUID, val createdAt: Instant, val modifiedAt: Instant, val version: Int,
        val authorId: UUID,
        val title: String, val status: BookStatus, val price: BigDecimal
)

enum class BookStatus { NEW, PUBLISHED; }

fun BookTable.rowToBookRecord(row: ResultRow): BookRecord =
        BookRecord(
                id = row[id], createdAt = row[createdAt], modifiedAt = row[modifiedAt], version = row[version],
                authorId = row[authorId],
                title = row[title], status = row[status], price = row[price]
        )
