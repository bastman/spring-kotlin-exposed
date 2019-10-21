package com.example.api.places.common.rest.response

import com.example.api.places.common.db.PlaceRecord
import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class PlaceDto(
        // pk
        val placeId: UUID,
        // record meta
        val createdAt: Instant,
        val modified_at: Instant,
        val deletedAt: Instant?,
        val active: Boolean,
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

fun PlaceRecord.toPlaceDto(): PlaceDto =
        PlaceDto(
                placeId = place_id,
                createdAt = createdAt,
                modified_at = modified_at,
                deletedAt = deleted_at,
                active = active,
                placeName = placeName,
                countryName = countryName,
                cityName = cityName,
                postalCode = postalCode,
                streetAddress = streetAddress,
                formattedAddress = formattedAddress,
                latitude = latitude,
                longitude = longitude
        )
