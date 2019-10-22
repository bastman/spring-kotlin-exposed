package com.example.util.exposed.functions.postgres

import com.example.util.exposed.functions.common.CustomBooleanFunction
import org.jetbrains.exposed.sql.*

/**
 * PostGIS or Cube + EarthDistance
 *
 * see:
 * https://gist.github.com/norman/1535879
 * https://developpaper.com/using-postgresql-database-to-app-geographical-location/
 */

// see: https://www.postgresql.org/docs/8.3/earthdistance.html
// ll_to_earth( place.latitude , place.longitude )
// ll_to_earth(float8, float8): earth - Returns the location of a point on the surface of the Earth given its latitude (argument 1) and longitude (argument 2) in degrees.


fun ll_to_earth(latitude: Expression<*>, longitude: Expression<*>): CustomFunction<Boolean?> = CustomBooleanFunction(
        functionName = "ll_to_earth",
        postfix = "",
        params = *(listOf(latitude, longitude).toTypedArray())
)

fun ll_to_earth(latitude: Double, longitude: Double): CustomFunction<Boolean?> =
        ll_to_earth(latitude = doubleParam(latitude), longitude = doubleParam(longitude))

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

//
/**
 * earth_box(earth, float8):cube - Returns a box suitable for an indexed search using the cube @> operator for points within a given great circle distance of a location. Some points in this box are further than the specified great circle distance from the location, so a second check using earth_distance should be included in the query.
 * https://www.postgresql.org/docs/10/earthdistance.html
 */
fun earth_box(earth: Expression<*>, earthDistance: Expression<*>): CustomFunction<Any> {
    val params = listOf(
            earth, earthDistance
    )
    val fn = CustomFunction<Any>("earth_box", BooleanColumnType(), *(params).toTypedArray())
    return fn
}


fun floatParam(value: Float): Expression<Float> = QueryParameter(value, FloatColumnType())
fun doubleParam(value: Double): Expression<Double> = QueryParameter(value, DoubleColumnType())


fun <T> CustomFunction<T>.pgContains(other: Expression<*>): Op<Boolean> = PgContainsOp(this, containsOtherExpr = other)
private class PgContainsOp(val sourceExpr: Expression<*>, val containsOtherExpr: Expression<*>) : ComparisonOp(sourceExpr, containsOtherExpr, "@>")
