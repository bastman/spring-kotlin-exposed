package com.example.api.places.db

import com.example.api.common.rest.error.exception.EntityNotFoundException
import com.example.api.places.common.db.PlaceRecord
import com.example.api.places.common.db.PlaceRepo
import com.example.api.places.fixtures.PlacesApiFixtures
import com.example.testutils.assertions.shouldEqualRecursively
import com.example.testutils.spring.BootWebMockMvcTest
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
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

    @Test
    fun `basic crud ops should work`() {
        val recordNew: PlaceRecord = PlacesApiFixtures.newPlaceRecord()
        val recordId: UUID = recordNew.place_id

        repo.insert(recordNew)
                .also { it shouldEqualRecursively recordNew }
        listOf(true, null)
                .onEach { isActive ->
                    repo.findById(placeId = recordId, isActive = isActive)
                            .also { it shouldEqualRecursively recordNew }
                }
                .onEach { isActive ->
                    repo.getById(placeId = recordId, isActive = isActive)
                            .also { it shouldEqualRecursively recordNew }
                }

        listOf(false)
                .onEach { isActive ->
                    repo.findById(placeId = recordId, isActive = isActive)
                            .also { it shouldBe null }
                }
                .onEach { isActive ->
                    assertThrows<EntityNotFoundException> { repo.getById(placeId = recordId, isActive = isActive) }
                }

        val toUpdate: PlaceRecord = recordNew.copy(
                active = false, modified_at = Instant.now(), deleted_at = Instant.now()
        )
        val recordUpdated: PlaceRecord = repo.update(record = toUpdate)
                .also { it shouldEqualRecursively toUpdate }
                .also { repo.getById(placeId = recordId, isActive = null) shouldEqualRecursively it }

        listOf(false, null)
                .onEach { isActive ->
                    repo.findById(placeId = recordId, isActive = isActive)
                            .also { it shouldEqualRecursively recordUpdated }
                }
                .onEach { isActive ->
                    repo.getById(placeId = recordId, isActive = isActive)
                            .also { it shouldEqualRecursively recordUpdated }
                }
        listOf(true)
                .onEach { isActive ->
                    repo.findById(placeId = recordId, isActive = isActive)
                            .also { it shouldBe null }
                }
                .onEach { isActive ->
                    assertThrows<EntityNotFoundException> { repo.getById(placeId = recordId, isActive = isActive) }
                }
    }

}
