package com.example.api.bookstore

import com.example.api.bookstore.domain.repo.Author
import com.example.api.bookstore.domain.repo.AuthorRepository
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
class BookStoreApiController(private val authorRepo: AuthorRepository) {

    @GetMapping("/api/bookstore/author")
    fun authorsFindAll() = authorRepo.findAll()

    @GetMapping("/api/bookstore/author/{id}")
    fun authorsGetOne(@PathVariable id: UUID) =
            authorRepo.requireOneById(id)

    @PutMapping("/api/bookstore/author")
    fun authorsCreateOne(@RequestBody req: CreateAuthorRequest): Author =
            authorRepo.insert(req.toRecord())

    @PostMapping("/api/bookstore/author/{id}")
    fun updateOne(@PathVariable id: UUID, @RequestBody req: UpdateAuthorRequest): Author {
        val record = authorRepo.requireOneById(id)
                .copy(modifiedAt = Instant.now(), name = req.name)

        return authorRepo.update(record)
    }
}


data class CreateAuthorRequest(val name: String)
data class UpdateAuthorRequest(val name: String)

private fun CreateAuthorRequest.toRecord(): Author {
    val now = Instant.now()
    return Author(
            id = UUID.randomUUID(),
            version = 0,
            createdAt = now,
            modifiedAt = now,
            name = name
    )
}