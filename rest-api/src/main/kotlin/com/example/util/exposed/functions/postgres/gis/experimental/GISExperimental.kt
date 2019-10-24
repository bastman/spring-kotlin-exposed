package com.example.util.exposed.functions.postgres.gis.experimental

import com.example.api.places.common.db.PlaceTable
import com.example.util.exposed.functions.common.CustomBooleanFunction
import com.example.util.exposed.functions.postgres.doubleParam
import org.jetbrains.exposed.sql.*
import org.postgresql.util.*

class NullExpr: Expression<Unit>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        append(" NULL ")
    }
}

class PgObjectColumnType: ColumnType() {
    override fun sqlType(): String  = "PGObject"

    override fun valueFromDB(value: Any): Any {
        val valueFromDB = super.valueFromDB(value)
        return when (valueFromDB) {
            is PGobject -> valueFromDB
            else -> valueFromDB
        }
    }
}
data class PGEarthPointLocation(val a:Double, val b:Double, val c:Double)
class PGEarthPointLocationColumnType: ColumnType() {
    override fun sqlType(): String  = "cube"

    override fun valueFromDB(value: Any): PGEarthPointLocation {
        value as PGobject
        val s=value.value
        val t = PGtokenizer(PGtokenizer.removePara(s), ',')
        return try {
            PGEarthPointLocation(
                    a=java.lang.Double.parseDouble(t.getToken(0)),
                    b = java.lang.Double.parseDouble(t.getToken(1)),
                    c=java.lang.Double.parseDouble(t.getToken(2))
            )
        } catch (e: NumberFormatException) {
            throw PSQLException(GT.tr("Conversion to type ${value.type} failed: $s."),
                    PSQLState.DATA_TYPE_MISMATCH, e)
        }

    }
}



fun <T:Number>ll_to_earth_exp(latitude:  T, longitude:  T)= CustomFunction<PGEarthPointLocation>(
        "ll_to_earth",
        PGEarthPointLocationColumnType(),
        doubleParam(latitude.toDouble()),
        doubleParam(longitude.toDouble())
)
@JvmName("ll_to_earth_exp_nullable")
fun <T:Number?>ll_to_earth_exp(latitude:  T?, longitude:  T?): CustomFunction<PGEarthPointLocation?> =
        CustomFunction<PGEarthPointLocation?>(
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

fun <T:Number>ll_to_earth_exp(latitude: Column<T>, longitude: Column<T>)= CustomFunction<PGEarthPointLocation>(
        "ll_to_earth",
        PGEarthPointLocationColumnType(),
        latitude,
        longitude
)
@JvmName("ll_to_earth_exp_by_cols_nullable")
fun <T:Number?>ll_to_earth_exp(latitude: Column<in T>, longitude: Column<in T>)= CustomFunction<PGEarthPointLocation?>(
        "ll_to_earth",
        PGEarthPointLocationColumnType().apply { nullable=true },
        latitude,
        longitude
)

/*
val a=Coalesce()

fun <T:PGEarthPointLocation>earth_distance_exp(from:  T, to:  T)= CustomFunction<PGEarthPointLocation>(
        "ll_to_earth",
        DoubleColumnType(),
        from,
        doubleParam(longitude.toDouble())
)


 */
