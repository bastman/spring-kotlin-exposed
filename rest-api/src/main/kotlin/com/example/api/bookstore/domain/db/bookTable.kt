package com.example.api.bookstore.domain.db

import com.example.util.exposed.instant
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
    val price = decimal("price",15,2)
}

data class BookRecord(
        val id: UUID,
        val createdAt: Instant,
        val modifiedAt: Instant,
        val version: Int,
        val authorId: UUID,
        val title: String,
        val status: BookStatus,
        val price: BigDecimal
)

enum class BookStatus { NEW, PUBLISHED; }

fun ResultRow.toBookRecord() =
        BookRecord(
                id = this[BookTable.id],
                createdAt = this[BookTable.createdAt],
                modifiedAt = this[BookTable.modifiedAt],
                version = this[BookTable.version],
                title = this[BookTable.title],
                status = this[BookTable.status],
                authorId = this[BookTable.authorId],
                price = this[BookTable.price]
        )
