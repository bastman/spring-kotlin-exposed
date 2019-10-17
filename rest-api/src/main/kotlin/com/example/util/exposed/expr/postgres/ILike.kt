package com.example.util.exposed.expr.postgres

import org.jetbrains.exposed.sql.*

// see: https://github.com/JetBrains/Exposed/issues/622


class InsensitiveLikeOp(expr1: Expression<*>, expr2: Expression<*>) : ComparisonOp(expr1, expr2, "ILIKE")

infix fun<T:String?> ExpressionWithColumnType<T>.ilike(pattern: String): Op<Boolean> = InsensitiveLikeOp(this, QueryParameter(pattern, columnType))

