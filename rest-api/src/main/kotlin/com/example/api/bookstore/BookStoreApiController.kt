package com.example.api.bookstore

import com.example.api.bookstore.domain.db.AuthorRecord
import com.example.api.bookstore.domain.db.BookRecord
import com.example.api.bookstore.domain.repo.AuthorRepository
import com.example.api.bookstore.domain.repo.BookRepository
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
class BookStoreApiController(
        private val authorRepo: AuthorRepository,
        private val bookRepo: BookRepository
) {

    @GetMapping("/api/bookstore/author")
    fun authorsFindAll() = authorRepo.findAll()

    @GetMapping("/api/bookstore/author/{id}")
    fun authorsGetOne(@PathVariable id: UUID) =
            authorRepo.requireOneById(id)

    @PutMapping("/api/bookstore/author")
    fun authorsCreateOne(@RequestBody req: AuthorCreateRequest) =
            authorRepo.insert(req.toRecord())

    @PostMapping("/api/bookstore/author/{id}")
    fun authorsUpdateOne(@PathVariable id: UUID, @RequestBody req: AuthorUpdateRequest): AuthorRecord {
        val record = authorRepo.requireOneById(id)
                .copy(modifiedAt = Instant.now(), name = req.name)

        return authorRepo.update(record)
    }

    @GetMapping("/api/bookstore/book")
    fun booksFindAll() = bookRepo.findAllBooksJoinAuthor().map { it.toBookDto() }

    @GetMapping("/api/bookstore/book/{id}")
    fun booksGetOne(@PathVariable id: UUID) =
            bookRepo.requireOneById(id)

    @PutMapping("/api/bookstore/book")
    fun booksCreateOne(@RequestBody req: BookCreateRequest) =
            bookRepo.insert(req.toRecord())

    @PostMapping("/api/bookstore/book/{id}")
    fun booksUpdateOne(@PathVariable id: UUID, @RequestBody req: BookUpdateRequest): BookRecord {
        val record = bookRepo.requireOneById(id)
                .copy(modifiedAt = Instant.now(), status = req.status, title = req.title, price = req.price)

        return bookRepo.update(record)
    }
}


