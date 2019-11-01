package com.example.util.exposed.postgres.extensions.earthdistance

import org.jetbrains.exposed.sql.*

/**
 * select ll_to_earth( 11.1 , 20.0 ); -> returns (5881394.65979286, 2140652.5921368, 1227937.44619261)
 * fun ll_to_earth(latitude:Double?, longitude:Double?):PGEarthPointLocation?
 */

fun <T : Number?> ll_to_earth(latitude: T, longitude: T): CustomFunction<PGEarthPointLocation?> =
        _ll_to_earth(latitude = latitude, longitude = longitude, returnsNullable = true)

@JvmName("ll_to_earth_not_nullable")
@Suppress("UNCHECKED_CAST")
fun <T : Number> ll_to_earth(latitude: T, longitude: T): CustomFunction<PGEarthPointLocation> =
        _ll_to_earth(
                latitude = latitude,
                longitude = longitude,
                returnsNullable = false
        ) as CustomFunction<PGEarthPointLocation>

private fun <T : Number?> _ll_to_earth(
        latitude: T?,
        longitude: T?,
        returnsNullable: Boolean
): CustomFunction<PGEarthPointLocation?> =
        CustomFunction(
                "ll_to_earth",
                PGEarthPointLocationColumnType().apply { nullable = returnsNullable },
                when (latitude) {
                    null -> NullExpr()
                    else -> doubleParam(latitude.toDouble())
                },
                when (longitude) {
                    null -> NullExpr()
                    else -> doubleParam(longitude.toDouble())
                }
        )


fun <T : Number?> ll_to_earth(latitude: Column<T>, longitude: Column<T>): CustomFunction<PGEarthPointLocation?> =
        _ll_to_earth(latitude = latitude, longitude = longitude)

@JvmName("ll_to_earth_not_nullable")
@Suppress("UNCHECKED_CAST")
fun <T : Number> ll_to_earth(latitude: Column<T>, longitude: Column<T>): CustomFunction<PGEarthPointLocation> =
        _ll_to_earth(latitude = latitude, longitude = longitude) as CustomFunction<PGEarthPointLocation>

fun <T : Number?> _ll_to_earth(
        latitude: Column<T>, longitude: Column<T>
): CustomFunction<PGEarthPointLocation?> = CustomFunction(
        "ll_to_earth",
        PGEarthPointLocationColumnType().apply { nullable = true },
        *(listOf(latitude, longitude).toTypedArray())
)

private fun doubleParam(value: Double): Expression<Double> = QueryParameter(value, DoubleColumnType())
