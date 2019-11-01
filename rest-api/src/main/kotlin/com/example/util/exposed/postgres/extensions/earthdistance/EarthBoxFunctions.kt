package com.example.util.exposed.postgres.extensions.earthdistance

import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.Expression

/**
 * earth_box(earth, float8):cube - Returns a box suitable for an indexed search using the cube @> operator for points within a given great circle distance of a location. Some points in this box are further than the specified great circle distance from the location, so a second check using earth_distance should be included in the query.
 * https://www.postgresql.org/docs/10/earthdistance.html
 *   select earth_box(ll_to_earth(1.0,2.0), 10); -> returns Box defined by Points: (6373301.75827338, 222550.95080971, 111304.380261276),(6373321.75827338, 222570.95080971, 111324.380261276)
 *   select earth_box(ll_to_earth(1.0,2.0), null); -> returns NULL
 *   select earth_box(null, 10); -> returns NULL
 */

/*
fun earth_box(earth: Expression<*>, earthDistance: Expression<*>): CustomFunction<Any> {
    val params = listOf(
            earth, earthDistance
    )
    val fn = CustomFunction<Any>("earth_box", BooleanColumnType(), *(params).toTypedArray())
    return fn
}

// CustomFunction<PGEarthPointLocation?>
 */
fun earth_boxV2(
        fromLocation: CustomFunction<out PGEarthPointLocation?>,
        greatCircleRadiusInMeter:Expression<out Number?>
):CustomFunction<PGEarthBox?>{
    val returnsNullable = true
     val params = listOf(
            fromLocation, greatCircleRadiusInMeter
    )
    val fn = CustomFunction<PGEarthBox?>(
            "earth_box",
            PGEarthBoxColumnType().apply { nullable=returnsNullable },
            *(params).toTypedArray()
    )
    return fn
}
