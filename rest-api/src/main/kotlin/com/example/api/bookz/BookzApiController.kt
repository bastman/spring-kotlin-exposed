package com.example.api.bookz

import com.example.api.bookz.db.BookzData
import com.example.api.bookz.db.BookzRecord
import com.example.api.bookz.db.BookzRepo
import com.example.api.bookz.handler.bulkSave.BulkSaveHandler
import mu.KLogging
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
class BookzApiController(
        private val repo: BookzRepo,
        private val bulkSaveHandler: BulkSaveHandler
) {

    // jsonb examples: see: https://www.compose.com/articles/faster-operations-with-the-jsonb-data-type-in-postgresql/

    @PutMapping("/api/$API_NAME/books")
    fun booksCreateOne(@RequestBody req: BookzCreateRequest): BookzDto =
            req.toBookzRecord(id = UUID.randomUUID(), now = Instant.now())
                    .let { repo.insert(record = it) }
                    .also { logger.info { "INSERT DB ENTITY: $it" } }
                    .toBookzDto()

    @GetMapping("/api/$API_NAME/books")
    fun booksFindAll(): List<BookzDto> =
            repo.findAll().map { it.toBookzDto() }

    @GetMapping("/api/$API_NAME/books/{id}")
    fun booksGetOne(@PathVariable id: UUID): BookzDto =
            repo[id].toBookzDto()

    @PostMapping("/api/$API_NAME/books/{id}")
    fun booksUpdateOne(@PathVariable id: UUID, @RequestBody req: BookzUpdateRequest): BookzDto =
            repo[id]
                    .copy(modifiedAt = Instant.now(), data = req.data)
                    .let { repo.update(record = it) }
                    .also { logger.info { "UPDATE DB ENTITY: $it" } }
                    .toBookzDto()

    @PostMapping("/api/$API_NAME/books/bulk-save")
    fun bulkSave():List<BookzDto> = bulkSaveHandler.handle(limit = 2)

    companion object : KLogging() {
        const val API_NAME = "bookz-jsonb"
    }
}