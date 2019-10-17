package com.example.util.exposed.expr.postgres

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.QueryBuilder

// see: https://github.com/JetBrains/Exposed/issues/500

/*
fun Column<*>.distinctOn(): Function<Int> = DistinctOn(this)

class DistinctOn(val expr: Expression<*>) : Function<Int>(IntegerColumnType()) {
    override fun toSQL(queryBuilder: QueryBuilder) = "DISTINCT ON (${expr.toSQL(queryBuilder)}) ${expr.toSQL(queryBuilder)}"
}

 */
