package com.example.api.bookz.handler.createOne

import com.example.api.bookz.BookzDto
import com.example.api.bookz.db.BookzRecord
import com.example.api.bookz.db.BookzRepo
import com.example.api.bookz.toBookzDto
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

private typealias Request = BookzCreateRequest
private typealias Response = BookzDto

@Component
class BookzCreateHandler(private val repo: BookzRepo) {

    @Transactional
    fun handle(req: Request): Response = req
            .let { insertIntoDb(it) }
            .let { mapToResponse(it) }

    private fun mapToResponse(it: BookzRecord) = it.toBookzDto()

    private fun insertIntoDb(req: Request) =
            repo.newRecord(id = UUID.randomUUID(), now = Instant.now(), data = req.data)
                    .let { repo.insert(record = it) }
                    .also { logger.info { "INSERT DB ENTITY: $it" } }

    companion object : KLogging()
}