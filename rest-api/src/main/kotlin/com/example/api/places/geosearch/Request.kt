package com.example.api.places.geosearch

import io.swagger.annotations.ApiModel
import java.util.*

private const val SWAGGER_PREFIX = "PlacesGeoSearchRequest"

data class PlacesGeoSearchRequest(
        val logId: UUID = UUID.randomUUID(),
        val payload: Payload
) {
    @ApiModel("${SWAGGER_PREFIX}_Payload")
    data class Payload(
            val latitude: Double,
            val longitude: Double,
            val radiusInMeter: Int,
            val limit: Int,
            val offset: Int
    )
}
