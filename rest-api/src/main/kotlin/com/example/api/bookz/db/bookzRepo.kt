package com.example.api.bookz.db

import com.example.util.exposed.crud.UUIDCrudRepo
import com.example.util.exposed.crud.UUIDCrudTable
import com.example.util.exposed.crud.updateRowById
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*


private typealias CrudTable = BookzTable
private typealias CrudRecord = BookzRecord

@Repository
@Transactional // Should be at @Service level in real applications
class BookzRepo : UUIDCrudRepo<UUIDCrudTable, CrudRecord>() {
    override val table = CrudTable
    override val mapr: (ResultRow) -> CrudRecord = ResultRow::toBookzRecord

    fun newRecord(id: UUID, data: BookzData, now: Instant): CrudRecord =
            CrudRecord(
                    id = id,
                    createdAt = now,
                    modifiedAt = now,
                    data = data
            )



    fun insert(record: BookzRecord): BookzRecord {
        BookzTable.insert({
            it[id] = record.crudRecordId()
            it[createdAt] = record.createdAt
            it[modifiedAt] = record.modifiedAt
            it[data] = record.data
        })
        return this[record.crudRecordId()]
    }

    fun updateOne(id: UUID, body: CrudTable.(UpdateStatement) -> Unit) =
            table.updateRowById(id, body = body)
                    .let { this[id] }

    fun insertOrUpdate(insertRecord: CrudRecord, updateStatement: CrudTable.(UpdateStatement) -> Unit): CrudRecord {
        val oldRecordId = findOne(insertRecord.crudRecordId())?.crudRecordId()
        return when (oldRecordId) {
            null -> insert(record = insertRecord)
            else -> updateOne(oldRecordId, updateStatement)
        }
    }

    fun findAll() =
            table.selectAll().map { mapr(it) }

}
