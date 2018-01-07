package com.example.api.tweeter.domain.db

import com.example.util.exposed.instant
import org.jetbrains.exposed.sql.Table

object Tweets : Table("tweet") {
    val id = uuid("id").primaryKey()
    val createdAt = instant("created_at")
    val modifiedAt = instant("updated_at")
    val version = integer("version")
    val message = text("message")
    val comment = text("comment").nullable()
}


