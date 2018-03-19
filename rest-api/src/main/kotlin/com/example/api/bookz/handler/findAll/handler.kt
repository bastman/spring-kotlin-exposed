package com.example.api.bookz.handler.findAll

import com.example.api.bookz.BookzDto
import com.example.api.bookz.db.BookzRecord
import com.example.api.bookz.db.BookzRepo
import com.example.api.bookz.toBookzDto
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private typealias Response = List<BookzDto>

@Component
class BookzFindAllHandler(
        private val repo: BookzRepo
) {

    @Transactional(readOnly = true)
    fun handle(): Response = loadFromDb()
            .let { mapToResponse(it) }

    private fun mapToResponse(items: List<BookzRecord>) = items
            .map { it.toBookzDto() }

    private fun loadFromDb(): List<BookzRecord> = repo.findAll()

}