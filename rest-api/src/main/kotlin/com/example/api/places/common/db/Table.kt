package com.example.api.places.common.db

import com.example.util.exposed.columnTypes.instant
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object PlaceTable : Table("place") {
    // pk
    val place_id = uuid("place_id")
    override val primaryKey: PrimaryKey = PrimaryKey(place_id, name = "place_pkey")
    // record meta
    val createdAt = instant("created_at")
    val modified_at = instant("modified_at")
    val deleted_at = instant("deleted_at").nullable()
    val active = bool(name = "active")
    // custom
    val placeName = varchar(name = "place_name", length = 255)
    val countryName = varchar(name = "country_name", length = 255)
    val cityName = varchar(name = "city_name", length = 2048)
    val postalCode = varchar(name = "postal_code", length = 255)
    val streetAddress = varchar(name = "street_address", length = 2048)
    val formattedAddress = varchar(name = "formatted_address", length = 2048)
    val latitude = decimal(name = "latitude", precision = 10, scale = 6)
    val longitude = decimal(name = "longitude", precision = 10, scale = 6)

    fun mapRowToRecord(row: ResultRow): PlaceRecord = PlaceRecord(
            // pk
            place_id = row[place_id],
            // record meta
            createdAt = row[createdAt],
            modified_at = row[modified_at],
            deleted_at = row[deleted_at],
            active = row[active],
            // custom
            placeName = row[placeName],
            countryName = row[countryName],
            cityName = row[cityName],
            postalCode = row[postalCode],
            streetAddress = row[streetAddress],
            formattedAddress = row[formattedAddress],
            latitude = row[latitude],
            longitude = row[longitude]
    )
}
