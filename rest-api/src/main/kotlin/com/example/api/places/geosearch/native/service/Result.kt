package com.akelius.offerb2b.bff.api.property.geosearch.service

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

