package com.example.api.bookz.handler.updateOneById

import com.example.api.bookz.db.BookzData
import java.util.*

data class BookzUpdateOneByIdRequest(val id: UUID, val data: BookzData)

data class BookzUpdateOnePayload(val data: BookzData)