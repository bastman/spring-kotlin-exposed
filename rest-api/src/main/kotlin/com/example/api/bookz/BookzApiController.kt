package com.example.api.bookz

import com.example.api.bookz.db.BookzRepo
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
class BookzApiController(private val repo: BookzRepo) {

    // see: https://www.compose.com/articles/faster-operations-with-the-jsonb-data-type-in-postgresql/

    @PutMapping("/api/bookz-jsonb/book")
    fun booksCreateOne(@RequestBody req: BookzCreateRequest): BookzDto =
            req.toBookzRecord(id = UUID.randomUUID(), now = Instant.now())
                    .let { repo.insert(record = it) }
                    .toBookzDto()

    @GetMapping("/api/bookz-jsonb/book")
    fun booksFindAll(): List<BookzDto> =
            repo.findAll().map { it.toBookzDto() }

    @GetMapping("/api/bookz-jsonb/book/{id}")
    fun booksGetOne(@PathVariable id: UUID): BookzDto =
            repo.requireOneById(id).toBookzDto()

    @PostMapping("/api/bookstore/book/{id}")
    fun booksUpdateOne(@PathVariable id: UUID, @RequestBody req: BookzUpdateRequest): BookzDto =
            repo.requireOneById(id)
                    .copy(modifiedAt = Instant.now(), data = req.data)
                    .let { repo.update(record = it) }
                    .toBookzDto()

}