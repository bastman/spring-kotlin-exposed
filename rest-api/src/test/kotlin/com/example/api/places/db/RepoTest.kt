package com.example.api.places.db

import com.example.api.common.rest.error.exception.EntityNotFoundException
import com.example.api.places.common.db.PlaceRepo
import com.example.testutils.spring.BootWebMockMvcTest
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class PlaceRepoTest(
        @Autowired private val repo: PlaceRepo
) : BootWebMockMvcTest() {

    @Test
    fun `unhappy - handle unknown id's `() {
        val unknownId: UUID = UUID.randomUUID()

        listOf(true, false, null).forEach { isActive ->
            repo.findById(placeId = unknownId, isActive = isActive) shouldBe null
            assertThrows<EntityNotFoundException> { repo.getById(placeId = unknownId, isActive = isActive) }
        }
    }
}
