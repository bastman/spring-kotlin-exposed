package com.example.api.places.fixtures

import com.example.api.places.common.db.PlaceRecord
import com.example.testutils.random.random
import com.example.testutils.random.randomBoolean
import com.example.testutils.random.randomString
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.Instant
import java.util.*

object PlacesApiFixtures {
    fun newPlaceRecord(place_id: UUID = UUID.randomUUID(), now: Instant = Instant.now()): PlaceRecord =
            PlaceRecord(
                    place_id = place_id,
                    createdAt = now,
                    modified_at = now,
                    deleted_at = null,
                    active = true,
                    placeName = "placeName",
                    countryName = "countryName",
                    cityName = "cityName",
                    postalCode = "postalCode",
                    streetAddress = "streetAddress",
                    formattedAddress = "formattedAddress",
                    latitude = BigDecimal("1.01"),
                    longitude = BigDecimal("2.02")

            )
}


fun PlaceRecord.randomized(preserveIds: Boolean, preserveIsActive: Boolean): PlaceRecord {
    val instantMin: Instant = Instant.EPOCH
    val instantMax: Instant = (Instant.now() + Duration.ofDays(50 * 365))
    return PlaceRecord(
            place_id = when (preserveIds) {
                true -> place_id
                false -> UUID.randomUUID()
            },
            createdAt = (instantMin..instantMax).random(),
            modified_at = (instantMin..instantMax).random(),
            deleted_at = listOf(null, (instantMin..instantMax).random()).shuffled().first(),
            active = when (preserveIsActive) {
                true -> active
                false -> randomBoolean()
            },
            placeName = randomString(prefix = "placeName"),
            countryName = randomString(prefix = "countryName"),
            cityName = randomString(prefix = "cityName"),
            postalCode = randomString(prefix = "postalCode"),
            streetAddress = randomString(prefix = "streetAddress"),
            formattedAddress = randomString(prefix = "formattedAddress"),
            latitude = (-180.0000..180.0000).random().toBigDecimal().setScale(4, RoundingMode.HALF_EVEN),
            longitude = (-180.0000..180.0000).random().toBigDecimal().setScale(4, RoundingMode.HALF_EVEN)
    )
}
