package com.example.api.bookz.db

import com.example.api.bookz.fixtures.BookzApiFixtures
import com.example.api.common.rest.error.exception.EntityNotFoundException
import com.example.testutils.assertions.shouldEqualRecursively
import com.example.testutils.spring.BootWebMockMvcTest
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*

class BookzRepoTest(
        @Autowired private val repo: BookzRepo
) : BootWebMockMvcTest() {

    @Test
    fun `unhappy - handle unknown id's `() {
        val unknownId: UUID = UUID.randomUUID()

        repo.findOne(id = unknownId) shouldBe null

        assertThrows<EntityNotFoundException> { repo.get(id = unknownId) }
        assertThrows<EntityNotFoundException> { repo[unknownId] }
    }

    @Test
    fun `basic crud ops should work`() {
        val recordNew: BookzRecord = BookzApiFixtures.newBookzRecord()
        val recordId: UUID = recordNew.id

        repo.insert(recordNew)
                .also { it shouldEqualRecursively recordNew }
        repo.get(id = recordId)
                .also { it shouldEqualRecursively recordNew }

        repo.findAll()
                .firstOrNull { it.id == recordId }
                .also { it shouldEqualRecursively recordNew }

        repo.findAllActive()
                .firstOrNull { it.id == recordId }
                .also { it shouldEqualRecursively recordNew }

        val toUpdate = recordNew.copy(isActive = false, modifiedAt = Instant.now())
        val recordUpdated = repo.updateOne(id = recordId) {
            it[modifiedAt] = toUpdate.modifiedAt
            it[isActive] = toUpdate.isActive
        }
                .also { it shouldEqualRecursively toUpdate }
                .also { repo[recordId] shouldEqualRecursively it }

        repo.get(id = recordId)
                .also { it shouldEqualRecursively recordUpdated }
        repo.findAll()
                .firstOrNull { it.id == recordId }
                .also { it shouldEqualRecursively recordUpdated }
        repo.findAllActive()
                .firstOrNull { it.id == recordId }
                .also { it shouldBe null }
    }
}
