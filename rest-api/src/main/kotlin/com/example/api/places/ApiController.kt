package com.example.api.places

import com.example.api.places.common.db.PlaceRecord
import com.example.api.places.common.db.PlaceRepo
import com.example.api.places.common.dto.ListResponseDto
import com.example.api.places.common.dto.PlaceDto
import com.example.api.places.common.dto.toPlaceDto
import mu.KLogging
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant
import java.util.*

private const val API_BASE_URI = "/api/places"

@RestController
class PlacesApiController(
        private val repo: PlaceRepo
) {
    companion object : KLogging()

    @GetMapping(API_BASE_URI)
    @Transactional(readOnly = true)
    fun findAll(): ListResponseDto<PlaceDto> = repo
            .findAll(isActive = true)
            .map { it.toPlaceDto() }
            .let { ListResponseDto(items = it) }

    @PutMapping(API_BASE_URI)
    @Transactional(readOnly = false)
    fun create(@RequestBody req: PlaceCreateInput): PlaceDto = req
            .toRecord(placeId = UUID.randomUUID(), now = Instant.now())
            .let(repo::insert)
            .also { logger.info { "INSERT DB ENTITY: $it" } }
            .toPlaceDto()
}

data class PlaceCreateInput(
        // custom
        val placeName: String,
        val countryName: String,
        val cityName: String,
        val postalCode: String,
        val streetAddress: String,
        val formattedAddress: String,
        val latitude: BigDecimal,
        val longitude: BigDecimal
)

private fun PlaceCreateInput.toRecord(placeId: UUID, now: Instant): PlaceRecord =
        PlaceRecord(
                place_id = placeId,
                createdAt = now,
                modified_at = now,
                deleted_at = null,
                active = true,
                placeName = placeName,
                countryName = countryName,
                cityName = cityName,
                postalCode = postalCode,
                streetAddress = streetAddress,
                formattedAddress = formattedAddress,
                latitude = latitude,
                longitude = longitude
        )
