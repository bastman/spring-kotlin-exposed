package com.example.api.bookstore.domain.repo

import com.example.api.bookstore.domain.db.AuthorRecord
import com.example.api.bookstore.domain.db.AuthorTable
import com.example.api.bookstore.domain.db.toAuthorRecord
import com.example.api.common.EntityNotFoundException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Repository
@Transactional // Should be at @Service level in real applications
class AuthorRepository {

    fun insert(author: AuthorRecord): AuthorRecord {
        AuthorTable.insert({
            it[id] = author.id
            it[createdAt] = author.createdAt
            it[modifiedAt] = author.modifiedAt
            it[version] = author.version
            it[name] = author.name
        })

        return requireOneById(author.id)
    }

    fun update(author: AuthorRecord): AuthorRecord {
        AuthorTable.update({ AuthorTable.id eq author.id }) {
            it[createdAt] = author.createdAt
            it[modifiedAt] = author.modifiedAt
            it[version] = author.version
            it[name] = author.name
        }

        return requireOneById(author.id)
    }

    fun requireOneById(id: UUID): AuthorRecord
            = getOneById(id) ?: throw EntityNotFoundException("AuthorRecord NOT FOUND ! (id=$id)")

    fun getOneById(id: UUID): AuthorRecord? =
            AuthorTable.select { AuthorTable.id eq id }
                    .limit(1)
                    .map { it.toAuthorRecord() }
                    .firstOrNull()

    fun findAll() = AuthorTable.selectAll().map { it.toAuthorRecord() }

}