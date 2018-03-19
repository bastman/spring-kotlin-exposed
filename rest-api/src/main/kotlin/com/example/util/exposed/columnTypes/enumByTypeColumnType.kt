package com.example.util.exposed.columnTypes

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGobject
import java.sql.PreparedStatement

fun <T : Any> Table.enumerationByNameAndSqlType(name: String, sqlType: String, klass: Class<T>): Column<T> =
        registerColumn(name, EnumBySqlType(sqlType, klass))

private class EnumBySqlType<out T : Any>(
        private val sqlType: String,
        private val klass: Class<T>
) : ColumnType() {
    override fun sqlType() = sqlType


    override fun notNullValueToDB(value: Any): Any = when (value) {
        is Enum<*> -> value.name
        else -> error("$value of ${value::class.qualifiedName} is not valid for enum ${klass.name}")
    }

    override fun valueFromDB(value: Any): Any = when (value) {
    //is Number -> klass.enumConstants!![value.toInt()]
        is Enum<*> -> value
        else -> error("$value of ${value::class.qualifiedName} is not valid for enum ${klass.name}")
    }

    override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = sqlType()
        obj.value = value as String
        stmt.setObject(index, obj)
    }
}