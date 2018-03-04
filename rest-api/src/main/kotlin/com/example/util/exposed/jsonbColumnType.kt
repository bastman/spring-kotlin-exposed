package com.example.util.exposed

// see: https://gist.github.com/quangIO/a623b5caa53c703e252d858f7a806919

import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGobject
import java.sql.PreparedStatement

/**
 * Created by quangio.
 */

fun <T : Any> Table.jsonb(name: String, klass: Class<T>, jsonMapper: ObjectMapper): Column<T> = registerColumn(name, JsonB(klass, jsonMapper))


private class JsonB<out T : Any>(private val klass: Class<T>, private val jsonMapper: ObjectMapper) : ColumnType() {
    override fun sqlType() = "jsonb"

    override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = value as String
        stmt.setObject(index, obj)
    }

    override fun valueFromDB(value: Any): Any {
        value as PGobject
        return try {
            jsonMapper.readValue(value.value, klass)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Can't parse JSON: $value")
        }
    }


    override fun notNullValueToDB(value: Any): Any = jsonMapper.writeValueAsString(value)
    override fun nonNullValueToString(value: Any): String = "'${jsonMapper.writeValueAsString(value)}'"
}