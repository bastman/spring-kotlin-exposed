package com.example.api.bookz.fixtures

import com.example.api.bookz.db.BookzData
import com.example.api.bookz.db.BookzRecord
import com.example.testutils.random.random
import com.example.testutils.random.randomBoolean
import com.example.testutils.random.randomString
import java.time.Duration
import java.time.Instant
import java.util.*

object BookzApiFixtures {
    fun newBookzRecord(bookzId: UUID = UUID.randomUUID(), now: Instant = Instant.now()): BookzRecord =
            BookzRecord(
                    id = bookzId,
                    createdAt = now,
                    modifiedAt = now,
                    isActive = true,
                    data = BookzData(
                            title = "title",
                            genres = listOf("genre-001", "genre-002"),
                            published = true
                    )
            )
}


fun BookzRecord.randomized(preserveIds: Boolean, preserveIsActive: Boolean): BookzRecord {
    val instantMin: Instant = Instant.EPOCH
    val instantMax: Instant = (Instant.now() + Duration.ofDays(50 * 365))
    return BookzRecord(
            id = when (preserveIds) {
                true -> id
                false -> UUID.randomUUID()
            },
            createdAt = (instantMin..instantMax).random(),
            modifiedAt = (instantMin..instantMax).random(),
            isActive = when (preserveIsActive) {
                true -> isActive
                false -> randomBoolean()
            },
            data = BookzData(
                    title = randomString(prefix = "title-"),
                    genres = (0..100).map { "genre-$it" }.shuffled().take((0..50).random()),
                    published = randomBoolean()
            )
    )
}
