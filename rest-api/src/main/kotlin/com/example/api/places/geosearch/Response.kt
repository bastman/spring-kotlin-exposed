package com.example.api.places.geosearch

import com.example.api.places.common.rest.response.ListResponseDto
import com.example.api.places.common.rest.response.PlaceDto

typealias PlacesGeoSearchResponse = ListResponseDto<PlacesGeoSearchResponseItem>

data class PlacesGeoSearchResponseItem(
        val distance: Double,
        val place: PlaceDto
)
