package com.example.api.places.geosearch.native.service

import java.util.*

data class GeoSearchServiceResult(
        val items: List<Item>
) {
    companion object {
        val EMPTY = GeoSearchServiceResult(items = emptyList())
    }

    data class Item(
            val placeId: UUID,
            val distance: Double
    )
}

