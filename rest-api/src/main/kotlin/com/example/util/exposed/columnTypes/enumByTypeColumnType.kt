package com.example.util.exposed.columnTypes

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGobject
import java.sql.PreparedStatement

fun <T : Enum<T>> Table.enumerationByNameAndSqlType(
        name: String,
        sqlType: String,
        klass: Class<T>,
        serialize: (toDb: Enum<*>)->String = { it.name },
        unserialize: (fromDb:String)->Enum<*> = {fromDb-> klass.enumConstants!!.first { it.name == fromDb }}
): Column<T> =
        registerColumn(name,
                EnumBySqlType(sqlType, klass, serialize, unserialize)
        )

private class EnumBySqlType<T : Enum<T>>(
        private val sqlType: String,
        private val klass: Class<T>,
        private val serialize: (toDb: Enum<*>)->String,
        private val unserialize: (fromDb:String)->Enum<*>
) : ColumnType() {
    override fun sqlType() = sqlType

    override fun notNullValueToDB(value: Any): Any = when (value) {
       // is String -> value
        is Enum<*> -> try {
            serialize(value)
        } catch (all:Exception) {
            error("$value of ${value::class.qualifiedName} is not valid for enum ${klass.name} . details: ${all.message}")
        }
        else -> error("$value of ${value::class.qualifiedName} is not valid for enum ${klass.name}")
    }

    override fun valueFromDB(value: Any): Any = when (value) {
        is String -> try {
            unserialize(value)
        }catch (all:Exception) {
            error("$value of ${value::class.qualifiedName} is not valid for enum ${klass.name} . details: ${all.message}")
        }
        //is Enum<*> -> value
        else -> error("$value of ${value::class.qualifiedName} is not valid for enum ${klass.name}")
    }

    override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = sqlType()
        obj.value = value as String
        stmt.setObject(index, obj)
    }
}