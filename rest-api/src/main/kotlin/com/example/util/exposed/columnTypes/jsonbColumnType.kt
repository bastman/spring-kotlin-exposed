package com.example.util.exposed.columnTypes

// see: https://gist.github.com/quangIO/a623b5caa53c703e252d858f7a806919

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject

/**
 * Created by quangio.
 */

fun <T : Any> Table.jsonb(name: String, klass: Class<T>, jsonMapper: ObjectMapper): Column<T> = registerColumn(name, JsonB(klass, jsonMapper))


private class JsonB<out T : Any>(private val klass: Class<T>, private val jsonMapper: ObjectMapper) : ColumnType() {
    override fun sqlType() = "jsonb"

    private fun valueToPGobject(value: Any?, index: Int): PGobject {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = when (value) {
            null -> null
            else -> value as String
        }
        return obj
    }

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj: PGobject = valueToPGobject(value = value, index = index)
        super.setParameter(stmt, index, obj)
    }

    /*
    override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = when(value) {
            null->null
            else->value as String
        }
        stmt.setObject(index, obj)
    }
     */

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

/**
 * created by: seb
 */

fun <T : Any> Table.jsonb(name: String, fromJson: (String) -> T, toJson: (T) -> String): Column<T> = registerColumn(name, JsonBFunctional(fromJson = fromJson, toJson = toJson))

private class JsonBFunctional<T : Any>(
        private val fromJson: (String) -> T,
        private val toJson: (T) -> String
) : ColumnType() {

    override fun sqlType() = "jsonb"

    private fun valueToPGobject(value: Any?, index: Int): PGobject {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = when (value) {
            null -> null
            else -> value as String
        }
        return obj
    }

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj: PGobject = valueToPGobject(value = value, index = index)
        super.setParameter(stmt, index, obj)
    }
    /*
    override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = when(value) {
            null->null
            else->value as String
        }
        stmt.setObject(index, obj)
    }

     */

    private fun jsonDecode(json: String) = fromJson.invoke(json)
    private fun jsonEncode(value: T) = toJson.invoke(value)

    override fun valueFromDB(value: Any): T {
        value as PGobject
        return try {
            jsonDecode(value.value?:"null")
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Can't parse JSON: $value")
        }
    }

    override fun notNullValueToDB(value: Any): String = jsonEncode(value as T)
    override fun nonNullValueToString(value: Any): String = "'${notNullValueToDB(value)}'"
}

fun <T : Any> Table.jsonb(name: String, typeRef: TypeReference<T>, jsonMapper: ObjectMapper): Column<T> = registerColumn(name, JsonBTypeRef(typeRef = typeRef, jsonMapper = jsonMapper))

private class JsonBTypeRef<T : Any>(
        private val typeRef: TypeReference<T>,
        private val jsonMapper: ObjectMapper
) : ColumnType() {

    override fun sqlType() = "jsonb"

    private fun valueToPGobject(value: Any?, index: Int): PGobject {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = when (value) {
            null -> null
            else -> value as String
        }
        return obj
    }

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj: PGobject = valueToPGobject(value = value, index = index)
        super.setParameter(stmt, index, obj)
    }

    /*
    override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = when(value) {
            null->null
            else->value as String
        }
        stmt.setObject(index, obj)
    }

     */

    private fun jsonDecode(json: String): T = jsonMapper.readValue(json, typeRef)
    private fun jsonEncode(value: T): String = jsonMapper.writeValueAsString(value)

    override fun valueFromDB(value: Any): T {
        value as PGobject
        return try {
            jsonDecode(value.value?:"null")
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Can't parse JSON: $value")
        }
    }

    override fun notNullValueToDB(value: Any): String = jsonEncode(value as T)
    override fun nonNullValueToString(value: Any): String = "'${notNullValueToDB(value)}'"
}
