package com.example.util.exposed.postgres.extensions.earthdistance

import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.QueryParameter

/**

select ll_to_earth( 11.1 , 20.0 ); -> returns (5881394.65979286, 2140652.5921368, 1227937.44619261)
select latitude('(5881394.65979286, 2140652.5921368, 1227937.44619261)'::earth); -> returns 11.1
fun ll_to_earth(latitude:Double?, longitude:Double?):PGEarthPointLocation?

 */

fun <T : PGEarthPointLocation?> latitude(earth: T): CustomFunction<Double?> = _latitude(earth)

@JvmName("latitude_not_nullable")
@Suppress("UNCHECKED_CAST")
fun latitude(earth: PGEarthPointLocation): CustomFunction<Double> = _latitude(earth) as CustomFunction<Double>

private fun <T : PGEarthPointLocation?> _latitude(earth: T): CustomFunction<Double?> {
    return CustomFunction(
            "latitude",
            DoubleColumnType().apply { nullable = earth == null },
            QueryParameter(earth, PGEarthPointLocationColumnType().apply { nullable = earth == null })
    )
}

fun <T : PGEarthPointLocation?> longitude(earth: T): CustomFunction<Double?> = _longitude(earth)
@JvmName("longitude_not_nullable")
@Suppress("UNCHECKED_CAST")
fun longitude(earth: PGEarthPointLocation): CustomFunction<Double> = _longitude(earth) as CustomFunction<Double>

private fun <T : PGEarthPointLocation?> _longitude(earth: T): CustomFunction<Double?> {
    return CustomFunction(
            "longitude",
            DoubleColumnType().apply { nullable = earth == null },
            QueryParameter(earth, PGEarthPointLocationColumnType().apply { nullable = earth == null })
    )
}


