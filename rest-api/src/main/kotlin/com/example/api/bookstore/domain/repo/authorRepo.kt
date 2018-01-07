package com.example.api.bookstore.domain.repo

import com.example.api.bookstore.domain.db.AuthorTable
import com.example.api.common.EntityNotFoundException
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

data class Author(
        val id: UUID,
        val createdAt: Instant,
        val modifiedAt: Instant,
        val version: Int,
        val name: String
)

@Repository
@Transactional // Should be at @Service level in real applications
class AuthorRepository {

    fun insert(author: Author): Author {
        AuthorTable.insert({
            it[id] = author.id
            it[createdAt] = author.createdAt
            it[modifiedAt] = author.modifiedAt
            it[version] = author.version
            it[name] = author.name
        })

        return requireOneById(author.id)
    }

    fun update(author: Author): Author {
        AuthorTable.update({ AuthorTable.id eq author.id }) {
            it[createdAt] = author.createdAt
            it[modifiedAt] = author.modifiedAt
            it[version] = author.version
            it[name] = author.name
        }

        return requireOneById(author.id)
    }

    fun requireOneById(id: UUID): Author
            = getOneById(id) ?: throw EntityNotFoundException("AuthorRecord NOT FOUND ! (id=$id)")

    fun getOneById(id: UUID): Author? =
            AuthorTable.select { AuthorTable.id eq id }
                    .limit(1)
                    .map { it.toAuthor() }
                    .firstOrNull()

    fun findAll() = AuthorTable.selectAll().map { it.toAuthor() }

    private fun ResultRow.toAuthor() =
            Author(
                    id = this[AuthorTable.id],
                    createdAt = this[AuthorTable.createdAt],
                    modifiedAt = this[AuthorTable.modifiedAt],
                    version = this[AuthorTable.version],
                    name = this[AuthorTable.name]
            )
}