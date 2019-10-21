package com.example.util.exposed.nativesql

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import java.sql.ResultSet
import java.sql.ResultSetMetaData

object NativeSql: INativeSql

interface INativeSql {
    fun <T : Any> sqlExecAndMap(sql:String, transaction: Transaction, transform: (ResultSet) -> T): List<T> {
        val result = arrayListOf<T>()
        transaction.exec(sql) { rs ->
            try {
                while (rs.next()) {
                    result += transform(rs)
                }
            } finally {
                rs.closeSilently()
            }

        }
        return result
    }

    private fun ResultSet.closeSilently() =
            try {
                when(isClosed) {
                    true-> Unit
                    false-> close()
                }
            } catch (all:Throwable) {
                // ignore
            }


    fun ResultSetMetaData.toQualifiedColumnIndexMap():Map<String, Int> {
        val meta:Map<String,Int> =(1..columnCount).map { colPos->
            val colLabel:String = getColumnLabel(colPos)
            val colTableName:String = getTableName(colPos)
            val key:String= listOf(colTableName, colLabel)
                    .filter { it.isNotEmpty()}
                    .joinToString(".")
            Pair(key, colPos)
        }.toMap()

        return meta
    }

    val Column<*>.qName:String
        get() = "${table.qTableName}.${name.toLowerCase()}"
    val Table.qTableName:String
        get() = tableName.toLowerCase()

}

