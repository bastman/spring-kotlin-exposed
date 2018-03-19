package com.example.api.bookz.handler.getOneById

import com.example.api.bookz.BookzDto
import com.example.api.bookz.db.BookzRecord
import com.example.api.bookz.db.BookzRepo
import com.example.api.bookz.toBookzDto
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private typealias Request = BookzGetOneByIdRequest
private typealias Response = BookzDto

@Component
class BookzGetOneByIdHandler(private val repo: BookzRepo) {

    @Transactional(readOnly = true)
    fun handle(req: Request): Response = req
            .let { loadFromDb(it) }
            .let { mapToResponse(it) }

    private fun mapToResponse(it: BookzRecord) = it.toBookzDto()
    private fun loadFromDb(req: Request): BookzRecord = repo[req.id]
}
