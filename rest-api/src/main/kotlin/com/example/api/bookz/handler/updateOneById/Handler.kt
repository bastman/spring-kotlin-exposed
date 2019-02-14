package com.example.api.bookz.handler.updateOneById

import com.example.api.bookz.BookzDto
import com.example.api.bookz.db.BookzRecord
import com.example.api.bookz.db.BookzRepo
import com.example.api.bookz.toBookzDto
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

private typealias Request = BookzUpdateOneByIdRequest
private typealias Response = BookzDto

@Component
class BookzUpdateOneByIdHandler(private val repo: BookzRepo) {

    @Transactional
    fun handle(req: Request): Response = req
            .let { saveToDb(it) }
            .let { mapToResponse(it) }

    private fun mapToResponse(it: BookzRecord) = it.toBookzDto()

    private fun saveToDb(req: Request) =
            repo.updateOne(req.id) {
                it[modifiedAt] = Instant.now()
                it[data] = req.data
            }
                    .also { logger.info { "UPDATE DB ENTITY: $it" } }

    companion object : KLogging()

}