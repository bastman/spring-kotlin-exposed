package com.example.util.exposed.postgres.extensions.earthdistance

import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.DoubleColumnType

/**
 * PostGIS or Cube + EarthDistance
 *
 * see:
 * https://hashrocket.com/blog/posts/juxtaposing-earthdistance-and-postgis
 * https://gist.github.com/norman/1535879
 * https://developpaper.com/using-postgresql-database-to-app-geographical-location/
 */

// select earth() -> returns the assumed radius of te earth as float8 ( SELECT '6378168'::float8 )
fun earth(): CustomFunction<Double> {
    val fn = CustomFunction<Double>("earth", DoubleColumnType())
    return fn
}


/**
 * earth_distance(earth, earth):float8
 * - Returns the great circle distance between two points on the surface of the Earth.
 * - returns a value in meters
 * see: https://www.postgresql.org/docs/8.3/earthdistance.html
 */

@JvmName("earth_distance_not_nullable")
@Suppress("UNCHECKED_CAST")
fun <T : PGEarthPointLocation> earth_distance(
        fromEarth: CustomFunction<T>, toEarth: CustomFunction<T>
): CustomFunction<Double> = _earth_distance(
        fromEarth = fromEarth,
        toEarth = toEarth,
        returnsNullable = false
) as CustomFunction<Double>

fun <T : PGEarthPointLocation?> earth_distance(
        fromEarth: CustomFunction<T>, toEarth: CustomFunction<T>
): CustomFunction<Double?> = _earth_distance(
        fromEarth = fromEarth,
        toEarth = toEarth,
        returnsNullable = true
)

private fun <T : PGEarthPointLocation?> _earth_distance(
        fromEarth: CustomFunction<T>,
        toEarth: CustomFunction<T>,
        returnsNullable: Boolean
): CustomFunction<Double?> {
    val params = listOf(
            fromEarth, toEarth
    )
    val fn = CustomFunction<Double?>(
            "earth_distance",
            DoubleColumnType().apply { nullable = returnsNullable },
            *(params).toTypedArray()
    )
    return fn
}

