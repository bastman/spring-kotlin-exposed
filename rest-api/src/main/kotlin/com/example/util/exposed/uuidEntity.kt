package com.example.util.exposed

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

open class UUIDIdTable(name: String = "", columnName: String = "id") : IdTable<UUID>(name) {
    override val id: Column<EntityID<UUID>> = uuid(columnName).primaryKey().entityId()
}

abstract class UUIDEntity(id: EntityID<UUID>) : Entity<UUID>(id)

abstract class UUIDEntityClass<out E : UUIDEntity>(table: IdTable<UUID>, entityType: Class<E>? = null) : EntityClass<UUID, E>(table, entityType)
