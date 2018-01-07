package com.example.api.bookstore.domain.repo.dao

import com.example.api.bookstore.domain.db.AuthorRecord
import com.example.api.bookstore.domain.db.dao.AuthorDAO
import com.example.api.bookstore.domain.db.dao.toAuthorRecord
import com.example.api.common.EntityNotFoundException
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Repository
@Transactional // Should be at @Service level in real applications
class AuthorDAORepository {

    fun insert(author: AuthorRecord): AuthorRecord {
        val dao = AuthorDAO.new(author.id) {
            createdAt = author.createdAt
            modifiedAt = author.modifiedAt
            version = author.version
            name = author.name
        }

        dao.flush()

        return requireOneById(author.id)
    }

    fun update(author: AuthorRecord): AuthorRecord {
        val dao = AuthorDAO.get(author.id)
                .apply {
                    modifiedAt = author.modifiedAt
                    version = author.version
                    name = author.name
                }

        dao.flush()

        return requireOneById(author.id)
    }

    fun getOneById(id: UUID): AuthorRecord? =
            AuthorDAO.findById(id)?.toAuthorRecord()

    fun requireOneById(id: UUID): AuthorRecord
            = getOneById(id) ?: throw EntityNotFoundException("AuthorRecord NOT FOUND ! (id=$id)")

    fun findAll() = AuthorDAO.all().map { it.toAuthorRecord() }

}