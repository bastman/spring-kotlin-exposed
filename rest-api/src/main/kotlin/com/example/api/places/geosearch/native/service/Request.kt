package com.example.api.places.geosearch.native.service

data class GeoSearchServiceRequest(
        val latitude: Double,
        val longitude: Double,
        val radiusInMeter: Int,
        val limit: Int,
        val offset: Int
)
