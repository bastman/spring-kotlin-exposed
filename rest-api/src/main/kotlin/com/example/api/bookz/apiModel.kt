package com.example.api.bookz

import com.example.api.bookz.db.BookzData
import com.example.api.bookz.db.BookzRecord
import java.time.Instant
import java.util.*

data class BookzDto(val id: UUID, val createdAt: Instant, val modifiedAt: Instant, val data: BookzData)
data class BookzCreateRequest(val data: BookzData)
data class BookzUpdateRequest(val data: BookzData)

fun BookzRecord.toBookzDto() =
        BookzDto(id = id, createdAt = createdAt, modifiedAt = modifiedAt, data = data)

fun BookzCreateRequest.toBookzRecord(id: UUID, now: Instant) =
        BookzRecord(id = id, createdAt = now, modifiedAt = now, data = data)