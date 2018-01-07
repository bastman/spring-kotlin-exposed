package com.example.api.bookstore.domain.db.dao

import com.example.api.bookstore.domain.db.AuthorRecord
import com.example.util.exposed.instant
import org.jetbrains.exposed.dao.EntityID
import java.util.*

object AuthorDAOTable : UUIDIdTable("author") {
    val createdAt = instant("created_at")
    val modifiedAt = instant("updated_at")
    val version = integer("version")
    val name = text("name")
}

class AuthorDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AuthorDAO>(AuthorDAOTable)

    var createdAt by AuthorDAOTable.createdAt
    var modifiedAt by AuthorDAOTable.modifiedAt
    var version by AuthorDAOTable.version
    var name by AuthorDAOTable.name
}

fun AuthorDAO.toAuthorRecord() = AuthorRecord(
        id = id.value,
        createdAt = createdAt,
        modifiedAt = modifiedAt,
        version = version,
        name = name
)