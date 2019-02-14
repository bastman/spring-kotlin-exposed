package com.example.util.exposed.crud

import org.jetbrains.exposed.sql.ResultRow
import java.util.*

abstract class AbstractRepo<ID : Any, out RECORD : Any>() {
    protected abstract val table: CrudRecordTable<ID, RECORD> //<ID:Any,out RECORD:Any>
    fun findOne(id: ID) = table.findOneById(id)
    operator fun get(id: ID) = table.getOneById(id)

}

abstract class UUIDCrudRepo<out TABLE : UUIDCrudTable, out RECORD : Any> {
    protected abstract val table: TABLE
    protected abstract val mapr: (ResultRow) -> RECORD

    fun findOne(id: UUID): RECORD? = table.findRowById(id)?.let(mapr)
    operator fun get(id: UUID): RECORD = table.getRowById(id).let(mapr)
    fun exists(id: UUID): Boolean = table.rowExistsById(id)
    fun mapRow(row: ResultRow): RECORD = mapr(row)
    fun mapRows(rows: List<ResultRow>): List<RECORD> = rows.map(mapr)
}