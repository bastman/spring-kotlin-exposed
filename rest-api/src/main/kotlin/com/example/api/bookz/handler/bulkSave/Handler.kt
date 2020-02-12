package com.example.api.bookz.handler.bulkSave

import com.example.api.bookz.BookzDto
import com.example.api.bookz.db.BookzData
import com.example.api.bookz.db.BookzRecord
import com.example.api.bookz.db.BookzRepo
import com.example.api.bookz.toBookzDto
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

private typealias Request = BookzBulkSaveRequest
private typealias Response = List<BookzDto>

@Component
class BulkSaveHandler(private val repo: BookzRepo) {
    private val ids = listOf(
            "c0c0d4aa-0de8-406a-9afa-8e72fe2e4739",
            "47c9bace-0066-48ac-80bf-0f3d57e99d33",
            "9c7dacc1-815f-48d7-89df-ae2f3a3006f4"
    ).map { UUID.fromString(it) }

    @Transactional
    fun handle(req: Request): Response = req
            .let { insertOrUpdateDb(it) }
            .let { mapToResponse(it) }

    private fun mapToResponse(records: List<BookzRecord>): Response =
            records.map { it.toBookzDto() }

    private fun insertOrUpdateDb(req: Request): List<BookzRecord> {
        val newRecords = (0..req.limit)
                .map {
                    val id = UUID.randomUUID()
                    repo.newRecord(
                            id = id, now = Instant.now(),
                            data = BookzData(
                                    published = true, title = "Some Title $id",
                                    genres = listOf("genreA", "genreB")
                            )
                    )
                }
                .also { logger.info { "INSERT DB ENTITY ...: $it" } }
                .map { repo.insert(it) }
                .also { logger.info { "INSERTED DB ENTITY: $it" } }

        val oldRecords = ids.map {
            when (val record = repo.findOne(it)) {
                null -> {
                    val id = UUID.randomUUID()
                    repo.newRecord(
                            id = id, now = Instant.now(),
                            data = BookzData(
                                    published = true, title = "Some Title $id",
                                    genres = listOf("genreA", "genreB")
                            )
                    )
                            .also { logger.info { "INSERT DB ENTITY ...: $it" } }
                            .let { repo.insert(it) }
                            .also { logger.info { "INSERTED DB ENTITY: $it" } }
                }
                else -> {
                    val now = Instant.now()
                    val id = record.id
                    repo.updateOne(id) {
                        it[modifiedAt] = now
                        it[data] = record.data.copy(title = "Some Title $id - updatedAt: $now")
                    }
                            .also { logger.info { "UPDATED DB ENTITY: $it" } }
                }
            }
        }

        val allRecords = newRecords + oldRecords

        return allRecords
    }

    companion object : KLogging()
}
