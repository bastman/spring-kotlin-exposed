package com.example.api.bookz

import com.example.api.bookz.handler.bulkSave.BookzBulkSaveRequest
import com.example.api.bookz.handler.bulkSave.BulkSaveHandler
import com.example.api.bookz.handler.createOne.BookzCreateHandler
import com.example.api.bookz.handler.createOne.BookzCreateRequest
import com.example.api.bookz.handler.findAll.BookzFindAllHandler
import com.example.api.bookz.handler.getOneById.BookzGetOneByIdHandler
import com.example.api.bookz.handler.getOneById.BookzGetOneByIdRequest
import com.example.api.bookz.handler.updateOneById.BookzUpdateOneByIdHandler
import com.example.api.bookz.handler.updateOneById.BookzUpdateOneByIdRequest
import com.example.api.bookz.handler.updateOneById.BookzUpdateOnePayload
import mu.KLogging
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class BookzApiController(
        private val createOne: BookzCreateHandler,
        private val updateOne: BookzUpdateOneByIdHandler,
        private val getOneById: BookzGetOneByIdHandler,
        private val findAll: BookzFindAllHandler,
        private val bulkSaveHandler: BulkSaveHandler
) {

    // jsonb examples: see: https://www.compose.com/articles/faster-operations-with-the-jsonb-data-type-in-postgresql/

    @PutMapping("/api/$API_NAME/books")
    fun booksCreateOne(@RequestBody req: BookzCreateRequest): BookzDto =
            BookzCreateRequest(data = req.data)
                    .let { createOne.handle(it) }

    @GetMapping("/api/$API_NAME/books")
    fun booksFindAll(): List<BookzDto> = findAll.handle()

    @GetMapping("/api/$API_NAME/books/{id}")
    fun booksGetOne(@PathVariable id: UUID): BookzDto = BookzGetOneByIdRequest(id = id)
            .let { getOneById.handle(it) }

    @PostMapping("/api/$API_NAME/books/{id}")
    fun booksUpdateOne(@PathVariable id: UUID, @RequestBody payload: BookzUpdateOnePayload): BookzDto =
            BookzUpdateOneByIdRequest(id = id, data = payload.data)
                    .let { updateOne.handle(it) }

    @PostMapping("/api/$API_NAME/books/bulk-save")
    fun bulkSave(): List<BookzDto> =
            BookzBulkSaveRequest(limit = 2)
                    .let { bulkSaveHandler.handle(it) }

    companion object : KLogging() {
        const val API_NAME = "bookz-jsonb"
    }
}