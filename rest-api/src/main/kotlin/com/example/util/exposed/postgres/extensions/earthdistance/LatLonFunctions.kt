package com.example.util.exposed.postgres.extensions.earthdistance

import com.example.api.places.common.db.PlaceTable
import com.example.util.exposed.functions.postgres.doubleParam
import org.jetbrains.exposed.sql.*
import java.math.BigDecimal
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

fun <T:PGEarthPointLocation?>latitude(earth:  T): CustomFunction<Double> = CustomFunction(
        "latitude",
        DoubleColumnType(),
        QueryParameter(earth, PGEarthPointLocationColumnType())
)
/*
fun CustomFunction<Double>.nullable() {
    val fn =this.functionName
    val t=this.columnType.apply { nullable=true }
    val params = this.expr
    // val newColumnType = Column<T?> (table, name, columnType)
    t.copy()
}

 */
