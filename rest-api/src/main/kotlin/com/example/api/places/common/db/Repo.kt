package com.example.api.places.common.db

import com.example.api.common.rest.error.exception.EntityNotFoundException
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Repository
@Transactional(propagation = Propagation.MANDATORY)
class PlaceRepo {
    private val table = PlaceTable

    fun findAll(isActive: Boolean?) = table
            .select {
                when (isActive) {
                    null -> Op.TRUE
                    else -> (table.active eq isActive)
                }
            }
            .map(table::mapRowToRecord)

    fun findByIdList(placeIds: Set<UUID>, isActive: Boolean?): List<PlaceRecord> = table
            .select {
                val op: Op<Boolean> = (table.place_id inList placeIds)
                when (isActive) {
                    null -> op
                    else -> op.and(table.active eq isActive)
                }
            }
            .map(table::mapRowToRecord)

    fun findById(placeId: UUID, isActive: Boolean?): PlaceRecord? = findByIdList(
            placeIds = setOf(placeId),
            isActive = isActive
    ).firstOrNull()

    fun getById(placeId: UUID, isActive: Boolean?): PlaceRecord = findById(placeId = placeId, isActive = isActive)
            ?: throw EntityNotFoundException("PlaceRecord not found ! (table: ${table.tableName} id: $placeId)")

    fun insert(record: PlaceRecord): PlaceRecord = table
            .insert {
                // pk
                it[place_id] = record.place_id
                // record meta
                it[createdAt] = record.createdAt
                it[modified_at] = record.modified_at
                it[deleted_at] = record.deleted_at
                it[active] = record.active
                // custom
                it[placeName] = record.placeName
                it[countryName] = record.countryName
                it[cityName] = record.cityName
                it[postalCode] = record.postalCode
                it[streetAddress] = record.streetAddress
                it[formattedAddress] = record.formattedAddress
                it[latitude] = record.latitude
                it[longitude] = record.longitude
            }.let { getById(placeId = record.place_id, isActive = null) }

    fun update(record: PlaceRecord): PlaceRecord = table
            .update({ table.place_id eq record.place_id }) {
                // pk
                // it[place_id] = record.place_id
                // record meta
                it[createdAt] = record.createdAt
                it[modified_at] = record.modified_at
                it[deleted_at] = record.deleted_at
                it[active] = record.active
                // custom
                it[placeName] = record.placeName
                it[countryName] = record.countryName
                it[cityName] = record.cityName
                it[postalCode] = record.postalCode
                it[streetAddress] = record.streetAddress
                it[formattedAddress] = record.formattedAddress
                it[latitude] = record.latitude
                it[longitude] = record.longitude
            }.let { getById(placeId = record.place_id, isActive = null) }

    private fun softDeleteById(placeId: UUID, deletedAt: Instant): PlaceRecord = table
            .update({ table.place_id eq placeId }) {
                it[active] = false
                it[deleted_at] = deletedAt
            }.let { getById(placeId = placeId, isActive = null) }

    private fun softRestoreById(placeId: UUID): PlaceRecord = table
            .update({ table.place_id eq placeId }) {
                it[active] = true
                it[deleted_at] = null
            }.let { getById(placeId = placeId, isActive = true) }
}

