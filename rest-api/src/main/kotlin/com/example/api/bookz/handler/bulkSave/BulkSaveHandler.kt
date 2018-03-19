package com.example.api.bookz.handler.bulkSave

import com.example.api.bookz.BookzApiController
import com.example.api.bookz.BookzDto
import com.example.api.bookz.db.BookzData
import com.example.api.bookz.db.BookzRecord
import com.example.api.bookz.db.BookzRepo
import com.example.api.bookz.toBookzDto
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Component
class BulkSaveHandler(private val repo: BookzRepo) {
    private val ids = listOf(
            "c0c0d4aa-0de8-406a-9afa-8e72fe2e4739",
            "47c9bace-0066-48ac-80bf-0f3d57e99d33",
            "9c7dacc1-815f-48d7-89df-ae2f3a3006f4"
    ).map { UUID.fromString(it) }

    @Transactional
    fun handle(limit: Int): List<BookzDto> = execBulk(limit = limit)
            .map { it.toBookzDto() }

    private fun execBulk(limit: Int): List<BookzRecord> {
        val newRecords = (0..limit)
                .map {
                    val id = UUID.randomUUID()
                    val now = Instant.now()
                    val data = BookzData(published = true, title = "Some Title $id", genres = listOf("genreA", "genreB"))
                    BookzRecord(id = id, createdAt = now, modifiedAt = now, data = data)
                }
                .also { BookzApiController.logger.info { "INSERT DB ENTITY ...: $it" } }
                .map { repo.insert(it) }
                .also { BookzApiController.logger.info { "INSERTED DB ENTITY: $it" } }

        val oldRecords = ids.map {
            val record = repo.findOneById(it)
            when (record) {
                null -> {
                    val id = UUID.randomUUID()
                    val now = Instant.now()
                    val data = BookzData(published = true, title = "Some Title $id", genres = listOf("genreA", "genreB"))
                    val newRecord = BookzRecord(id = id, createdAt = now, modifiedAt = now, data = data)
                            .also { BookzApiController.logger.info { "INSERT DB ENTITY ...: $it" } }
                    repo.insert(newRecord)
                }
                else -> {
                    val now = Instant.now()
                    val id = record.id
                    val updateRecord = record.copy(
                            modifiedAt = now,
                            data = record.data.copy("Some Title $id - updatedAt: $now")
                    )
                    repo.update(updateRecord)
                }
            }
        }

        val allRecords = newRecords + oldRecords

        return allRecords
    }
}