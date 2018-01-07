package com.example.util.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.DateColumnType
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime as JodaDateTime
import java.time.Instant as JavaInstant

fun Table.instant(name: String): Column<java.time.Instant>
        = registerColumn(name, InstantColumnType(true))

private fun JodaDateTime.toInstantJava() = JavaInstant.ofEpochMilli(this.millis)
private fun JavaInstant.toJodaDateTime() = JodaDateTime(this.toEpochMilli())


class InstantColumnType(time: Boolean) : ColumnType() {
    private val delegate = DateColumnType(time)

    override fun sqlType(): String = delegate.sqlType()

    override fun nonNullValueToString(value: Any): String = when (value) {
        is JavaInstant -> delegate.nonNullValueToString(value.toJodaDateTime())
        else -> delegate.nonNullValueToString(value)
    }

    override fun valueFromDB(value: Any): Any {
        val fromDb = when (value) {
            is JavaInstant -> delegate.valueFromDB(value.toJodaDateTime())
            else -> delegate.valueFromDB(value)
        }
        return when (fromDb) {
            is JodaDateTime -> fromDb.toInstantJava()
            else -> error("failed to convert value to Instant")
        }
    }

    override fun notNullValueToDB(value: Any): Any = when (value) {
        is JavaInstant -> delegate.notNullValueToDB(value.toJodaDateTime())
        else -> delegate.notNullValueToDB(value)
    }
}

