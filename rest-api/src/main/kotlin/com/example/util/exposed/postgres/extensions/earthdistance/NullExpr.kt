package com.example.util.exposed.postgres.extensions.earthdistance

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.QueryBuilder

class NullExpr : Expression<Unit>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        append(" NULL ")
    }
}
