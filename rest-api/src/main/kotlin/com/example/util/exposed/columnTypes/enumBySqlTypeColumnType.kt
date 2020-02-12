package com.example.util.exposed.columnTypes

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject

fun <T : Enum<T>> Table.enumerationBySqlType(
        name: String,
        sqlType: String,
        klass: Class<T>,
        serialize: (toDb: T) -> String = { it.name },
        unserialize: (fromDb: String, toKlass: Class<T>) -> T = { fromDb, toKlass ->
            toKlass.enumConstants.first { it.name == fromDb }
        }
): Column<T> =
        registerColumn(
                name = name,
                type = EnumBySqlType(sqlType, klass, serialize, unserialize)
        )

private class EnumBySqlType<T : Enum<T>>(
        private val sqlType: String,
        private val klass: Class<T>,
        private val serialize: (toDb: T) -> String,
        private val unserialize: (fromDb: String, toKlass: Class<T>) -> T
) : ColumnType() {
    override fun sqlType() = sqlType

    @Suppress("UNCHECKED_CAST")
    override fun notNullValueToDB(value: Any): Any = when (value) {
        // is String -> value
        is Enum<*> -> try {
            serialize(value as T)
        } catch (all: Exception) {
            error(
                    "SERIALIZE FAILED! $value of ${value::class.qualifiedName} is not valid for enum ${klass.name} ."
                            + " details: ${all.message}"
            )
        }
        else -> error("SERIALIZE FAILED! $value of ${value::class.qualifiedName} is not valid for enum ${klass.name}")
    }

    override fun valueFromDB(value: Any): Any = when (value) {
        is String -> try {
            unserialize(value, klass)
        } catch (all: Exception) {
            error(
                    "UNSERIALIZE FAILED! $value of ${value::class.qualifiedName} is not valid for enum ${klass.name} ."
                            + " details: ${all.message}"
            )
        }
        //is Enum<*> -> value
        else -> error("UNSERIALIZE FAILED! $value of ${value::class.qualifiedName} is not valid for enum ${klass.name}")
    }

    private fun valueToPGobject(value: Any?, index: Int): PGobject {
        val obj = PGobject()
        obj.type = sqlType()
        obj.value = value as String
        return obj
    }

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj: PGobject = valueToPGobject(value = value, index = index)
        super.setParameter(stmt, index, obj)
    }

    /*
    override fun setParameter(stmt: PreparedStatement, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = sqlType()
        obj.value = value as String
        stmt.setObject(index, obj)
    }

     */
}

/*
private inline fun <reified T : Enum<T>> EnumBySqlType<T>.fromDb(value: Any, unserialize: (fromDb:String)->T):T  = when (value) {
    is String -> try {
        unserialize(value)
    }catch (all:Exception) {
        error("$value of ${value::class.qualifiedName} is not valid for enum ${T::class.java.name} . details: ${all.message}")
    }
//is Enum<*> -> value
    else -> error("$value of ${value::class.qualifiedName} is not valid for enum ${T::class.java.name}")
}
*/

