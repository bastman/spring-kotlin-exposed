package com.example.util.exposed.postgres.extensions.earthdistance

import com.example.api.places.common.db.PlaceTable
import com.example.util.exposed.columnTypes.instant
import com.example.util.exposed.functions.postgres.doubleParam
import org.jetbrains.exposed.sql.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.Temporal
import org.jetbrains.exposed.sql.Function as ExposedFunction
/**

select ll_to_earth( 11.1 , 20.0 ); -> returns (5881394.65979286, 2140652.5921368, 1227937.44619261)
select latitude('(5881394.65979286, 2140652.5921368, 1227937.44619261)'::earth); -> returns 11.1

fun ll_to_earth(latitude:Double?, longitude:Double?):PGEarthPointLocation?

 */

fun <T:Number>ll_to_earth(latitude:  T, longitude:  T): CustomFunction<PGEarthPointLocation> = CustomFunction(
        "ll_to_earth",
        PGEarthPointLocationColumnType(),
        doubleParam(latitude.toDouble()),
        doubleParam(longitude.toDouble())
)
@JvmName("ll_to_earth_nullable")
fun <T:Number?>ll_to_earth_nullable(latitude:  T?, longitude:  T?): CustomFunction<PGEarthPointLocation?> =
        CustomFunction(
                "ll_to_earth",
                PGEarthPointLocationColumnType().apply { nullable=true },
                when(latitude) {
                    null-> NullExpr()
                    else -> doubleParam(latitude.toDouble())
                },
                when(longitude) {
                    null->  NullExpr()
                    else -> doubleParam(longitude.toDouble())
                }
        )

fun <T:Number?>ll_to_earth2(latitude:  T?, longitude:  T?): CustomFunction<PGEarthPointLocation?> =
        _ll_to_earth(latitude=latitude,longitude = longitude)
@JvmName("ll_to_earth2_not_nullable")
@Suppress("UNCHECKED_CAST")
fun <T:Number>ll_to_earth2(latitude:  T, longitude:  T): CustomFunction<PGEarthPointLocation> =
        _ll_to_earth(latitude=latitude,longitude = longitude) as CustomFunction<PGEarthPointLocation>

private fun <T:Number?>_ll_to_earth(latitude:  T?, longitude:  T?): CustomFunction<PGEarthPointLocation?> =
        CustomFunction(
                "ll_to_earth",
                PGEarthPointLocationColumnType().apply { nullable=true },
                when(latitude) {
                    null-> NullExpr()
                    else -> doubleParam(latitude.toDouble())
                },
                when(longitude) {
                    null->  NullExpr()
                    else -> doubleParam(longitude.toDouble())
                }
        )

@JvmName("latitude_not_nullable")
@Suppress("UNCHECKED_CAST")
fun latitude(earth: PGEarthPointLocation): CustomFunction<Double> = _latitude(earth) as CustomFunction<Double>
fun <T:PGEarthPointLocation?>latitude(earth: T): CustomFunction<Double?> = _latitude(earth)

private fun <T:PGEarthPointLocation?>_latitude(earth: T): CustomFunction<Double?> {
    return CustomFunction(
            "latitude",
            DoubleColumnType().apply { nullable = earth == null },
            QueryParameter(earth, PGEarthPointLocationColumnType().apply { nullable = earth == null })
    )
}



fun CustomFunction<Double?>.nn():CustomFunction<Double> {
    this.columnType.apply { nullable=false }
    return this as CustomFunction<Double>
}


