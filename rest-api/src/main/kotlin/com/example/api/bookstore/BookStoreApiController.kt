package com.example.api.bookstore

import com.example.api.bookstore.db.AuthorRepository
import com.example.api.bookstore.db.BookRepository
import mu.KLogging
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
class BookStoreApiController(private val authorRepo: AuthorRepository, private val bookRepo: BookRepository) {

    @GetMapping("/$API_AUTHORS")
    fun authorsFindAll(): List<AuthorDto> = authorRepo
            .findAll()
            .map { it.toAuthorDto() }

    @GetMapping("/$API_AUTHORS/{id}")
    fun authorsGetOne(@PathVariable id: UUID): AuthorDto = authorRepo[id].toAuthorDto()

    @PutMapping("/$API_AUTHORS")
    fun authorsCreateOne(@RequestBody req: AuthorCreateRequest): AuthorDto = req
            .toAuthorRecord(id = UUID.randomUUID(), now = Instant.now())
            .let { authorRepo.insert(it) }
            .also { logger.info { "INSERT DB ENTITY: $it" } }
            .toAuthorDto()

    @PostMapping("/$API_AUTHORS/{id}")
    fun authorsUpdateOne(@PathVariable id: UUID, @RequestBody req: AuthorUpdateRequest): AuthorDto =
            authorRepo[id]
                    .copy(modifiedAt = Instant.now(), name = req.name)
                    .let { authorRepo.update(it) }
                    .also { logger.info { "UPDATE DB ENTITY: $it" } }
                    .toAuthorDto()

    @GetMapping("/$API_BOOKS")
    fun booksFindAll(): List<BookWithAuthorDto> =
            bookRepo.findAllBooksJoinAuthor()
                    .map { it.toBookWithAuthorDto() }

    @GetMapping("/$API_BOOKS/{id}")
    fun booksGetOne(@PathVariable id: UUID): BookWithAuthorDto =
            bookRepo.requireOneJoinAuthor(id).toBookWithAuthorDto()

    @PutMapping("/$API_BOOKS")
    fun booksCreateOne(@RequestBody req: BookCreateRequest): BookWithAuthorDto =
            req.toBookRecord(id = UUID.randomUUID(), now = Instant.now())
                    .also { authorRepo.requireIdExists(req.authorId) }
                    .let { bookRepo.insert(it) }
                    .also { logger.info { "INSERT DB ENTITY: $it" } }
                    .let { bookRepo.requireOneJoinAuthor(it.id) }
                    .toBookWithAuthorDto()

    @PostMapping("/$API_BOOKS/{id}")
    fun booksUpdateOne(@PathVariable id: UUID, @RequestBody req: BookUpdateRequest): BookWithAuthorDto =
            bookRepo[id]
                    .copy(modifiedAt = Instant.now(), status = req.status, title = req.title, price = req.price)
                    .let { bookRepo.update(it) }
                    .also { logger.info { "UPDATE DB ENTITY: $it" } }
                    .let { bookRepo.requireOneJoinAuthor(it.id) }
                    .toBookWithAuthorDto()

    companion object : KLogging() {
        private const val API_AUTHORS = "api/bookstore/authors"
        private const val API_BOOKS = "api/bookstore/books"
    }
}


