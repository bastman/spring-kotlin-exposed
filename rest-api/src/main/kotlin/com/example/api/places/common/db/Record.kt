package com.example.api.places.common.db

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class PlaceRecord(
        // pk
        val place_id: UUID,
        // record meta
        val createdAt: Instant,
        val modified_at: Instant,
        val deleted_at: Instant?,
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
