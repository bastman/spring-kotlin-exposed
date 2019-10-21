package com.example.api.places.common.rest.mutation

import com.example.api.places.common.db.PlaceRecord
import io.swagger.annotations.ApiModel
import java.math.BigDecimal
import java.time.Instant
import java.util.*

private const val SWAGGER_PREFIX = "PlacesApiMutation"

sealed class Mutations {

    @ApiModel("${SWAGGER_PREFIX}_CreatePlace")
    data class CreatePlace(
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
}

fun Mutations.CreatePlace.toRecord(placeId: UUID, now: Instant): PlaceRecord =
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
