package com.example.util.exposed.postgres.extensions.earthdistance

import org.jetbrains.exposed.sql.ComparisonOp
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Op

/**
 * https://www.postgresql.org/docs/9.4/functions-range.html
 */

infix fun Expression<*>.rangeContains(other: Expression<*>): Op<Boolean> =
        PgRangeContainsOp(this, containsOtherExpr = other)

private class PgRangeContainsOp(val sourceExpr: Expression<*>, val containsOtherExpr: Expression<*>) :
        ComparisonOp(sourceExpr, containsOtherExpr, "@>")
