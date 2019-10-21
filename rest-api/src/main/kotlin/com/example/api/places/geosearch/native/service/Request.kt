package com.akelius.offerb2b.bff.api.property.geosearch.service

data class GeoSearchServiceRequest(
        val latitude: Double,
        val longitude: Double,
        val radiusInMeter: Int,
        val limit: Int,
        val offset: Int
)
