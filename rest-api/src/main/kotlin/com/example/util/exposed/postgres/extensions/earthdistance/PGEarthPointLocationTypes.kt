package com.example.util.exposed.postgres.extensions.earthdistance

import org.jetbrains.exposed.sql.ColumnType
import org.postgresql.util.*
import java.sql.PreparedStatement


data class PGEarthPointLocation(val x:Double, val y:Double, val z:Double)
class PGEarthPointLocationColumnType: ColumnType() {
    private val pgObjectType:String =  "cube"
    override fun sqlType(): String  = pgObjectType

    private fun PGEarthPointLocation.toPgValue():String =  "($x, $y, $z)"


    override fun valueFromDB(value: Any): PGEarthPointLocation {
        var pgTypeGiven:String?=null
        var pgValueGiven:String?=null
        return try {
            value as PGobject
            pgTypeGiven=value.type
            pgValueGiven=value.value
            val t = PGtokenizer(PGtokenizer.removePara(pgValueGiven), ',')
            PGEarthPointLocation(
                    x=java.lang.Double.parseDouble(t.getToken(0)),
                    y = java.lang.Double.parseDouble(t.getToken(1)),
                    z=java.lang.Double.parseDouble(t.getToken(2))
            )
        } catch (e: NumberFormatException) {
            throw PSQLException(
                    GT.tr("Conversion to type $pgTypeGiven -> ${this::class.qualifiedName} failed: $pgValueGiven."),
                    PSQLState.DATA_TYPE_MISMATCH, e)
        }
    }

    override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = sqlType()
        obj.value = when(value) {
            null->null
            else-> try {
                (value as PGEarthPointLocation).toPgValue()
            } catch (all:Exception) {
                throw PSQLException(
                        "Failed to setParameter at index: $index - value: $value ! reason: ${all.message}",
                        PSQLState.DATA_TYPE_MISMATCH,
                        all
                )
            }
        }
        stmt.setObject(index, obj)
    }

    override fun notNullValueToDB(value: Any): PGEarthPointLocation {
        return value as PGEarthPointLocation
    }
    override fun nonNullValueToString(value: Any): String {
        val sinkValue:PGEarthPointLocation = notNullValueToDB(value)
        return "'${sinkValue.toPgValue()}'"
    }
}
