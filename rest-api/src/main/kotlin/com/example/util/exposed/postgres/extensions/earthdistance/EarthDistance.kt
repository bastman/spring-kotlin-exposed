package com.example.util.exposed.postgres.extensions.earthdistance

import com.example.util.exposed.functions.postgres.doubleParam
import org.jetbrains.exposed.sql.*
import org.postgresql.util.*
import java.sql.PreparedStatement

// select earth() -> returns the assumed radius of te earth as float8 ( SELECT '6378168'::float8 )
fun earth(): CustomFunction<Double> {
    val fn = CustomFunction<Double>("earth", DoubleColumnType())
    return fn
}

/*
/**
 * earth_distance(earth, earth):float8
 * - Returns the great circle distance between two points on the surface of the Earth.
 * - returns a value in meters
 * see: https://www.postgresql.org/docs/8.3/earthdistance.html
 */
fun earth_distance(fromEarth: Expression<*>, toEarth: Expression<*>): CustomFunction<Double> {
    val params = listOf(
            fromEarth, toEarth
    )
    val fn = CustomFunction<Double>("earth_distance", DoubleColumnType(), *(params).toTypedArray())
    return fn
}
 */
@JvmName("earth_distance_not_nullable")
@Suppress("UNCHECKED_CAST")
fun <T:PGEarthPointLocation>earth_distanceV2(
        fromEarth: CustomFunction<T>, toEarth: CustomFunction<T>
): CustomFunction<Double> = _earth_distance(
        fromEarth=fromEarth,
        toEarth = toEarth,
        returnsNullable = false
) as CustomFunction<Double>

fun <T:PGEarthPointLocation?>earth_distanceV2(
        fromEarth: CustomFunction<T>, toEarth: CustomFunction<T>, returnsNullable:Boolean
): CustomFunction<Double?> = _earth_distance(fromEarth=fromEarth, toEarth = toEarth, returnsNullable = true)

private fun <T:PGEarthPointLocation?>_earth_distance(
        fromEarth: CustomFunction<T>, toEarth: CustomFunction<T>, returnsNullable:Boolean
): CustomFunction<Double?> {
    val params = listOf(
            fromEarth, toEarth
    )
    val fn = CustomFunction<Double?>(
            "earth_distance",
            DoubleColumnType().apply { nullable=returnsNullable },
            *(params).toTypedArray()
    )
    return fn
}

/**

select ll_to_earth( 11.1 , 20.0 ); -> returns (5881394.65979286, 2140652.5921368, 1227937.44619261)
select latitude('(5881394.65979286, 2140652.5921368, 1227937.44619261)'::earth); -> returns 11.1

fun ll_to_earth(latitude:Double?, longitude:Double?):PGEarthPointLocation?

 */



