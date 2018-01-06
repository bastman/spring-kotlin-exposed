package com.example.api.tweeter.domain.db

import org.jetbrains.exposed.sql.Table

object Tweets : Table("tweets") {
    val id = uuid("id").primaryKey()
    val createdAt = datetime("created_at")
    val modifiedAt = datetime("updated_at")
    val version = integer("version")
    val message = text("message")
    val comment = text("comment").nullable()
}

