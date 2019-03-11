package com.example.util.exposed.crud


import com.example.api.common.error.exceptions.EntityNotFoundException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateStatement
import java.util.*


/*
open class UUIDTable(name: String = "", columnName: String = "id") : IdTable<UUID>(name) {
    override val id: Column<EntityID<UUID>> = uuid(columnName).primaryKey()
            .clientDefault { UUID.randomUUID() }
            .entityId()
}

abstract class IdTable<T:Comparable<T>>(name: String=""): Table(name) {
    abstract val id : Column<EntityID<T>>

}
 */

abstract class IdCrudTable<ID : Comparable<ID>>(
        name: String = ""
) : Table(name) {
    abstract val crudIdColumn: () -> Column<ID>
    // abstract fun crudIdColumn():Column<ID>
    fun findRowById(id: ID) =
            select { crudIdColumn() eq id }
                    .limit(1)
                    .firstOrNull()

    fun getRowById(id: ID) = findRowById(id)
            ?: throw EntityNotFoundException("DB RECORD NOT FOUND ! (${crudIdColumn().name}=$id)")

    fun rowExistsById(id: ID): Boolean =
            select { crudIdColumn() eq id }
                    .limit(1)
                    .count() > 0
}

abstract class UUIDCrudTable(name: String = "") : IdCrudTable<UUID>(name)

fun <TABLE : UUIDCrudTable> TABLE.updateRowById(id: UUID, body: TABLE.(UpdateStatement) -> Unit) =
        update({ crudIdColumn() eq id }, body = body)

fun <TABLE : UUIDCrudTable> TABLE.updateRowByIdAndGet(id: UUID, body: TABLE.(UpdateStatement) -> Unit) =
        update({ crudIdColumn() eq id }, body = body)
                .let { getRowById(id) }

interface ICrudRecordTable<ID : Any, out RECORD : Any> {
    fun crudIdColumn(): Column<ID>
    val resultRowMapper: (row: ResultRow) -> RECORD

    fun findOneById(id: ID): RECORD?
    fun getOneById(id: ID): RECORD
    fun existsById(id: ID): Boolean
}


abstract class CrudRecordTable<ID : Any, out RECORD : Any>(
        name: String = ""
) : Table(name = name), ICrudRecordTable<ID, RECORD> {
    override fun findOneById(id: ID) =
            select { crudIdColumn().eq(id) }
                    .limit(1)
                    .map { resultRowMapper(it) }
                    .firstOrNull()

    override fun getOneById(id: ID) = findOneById(id)
            ?: throw EntityNotFoundException("DB RECORD NOT FOUND ! (${crudIdColumn().name}=$id)")

    override fun existsById(id: ID): Boolean =
            select { crudIdColumn() eq id }
                    .limit(1)
                    .count() > 0
}

fun <TABLE : CrudRecordTable<ID, RECORD>, ID : Any, RECORD : Any> TABLE.updateOneById(id: ID, body: TABLE.(UpdateStatement) -> Unit) =
        update({ crudIdColumn() eq id }, body = body)
                .let { getOneById(id) }