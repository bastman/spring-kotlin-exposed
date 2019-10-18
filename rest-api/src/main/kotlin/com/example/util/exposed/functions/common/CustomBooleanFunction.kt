package com.example.util.exposed.functions.common

import org.jetbrains.exposed.sql.BooleanColumnType
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.QueryBuilder

/**
 * Boolean Function (with optional postfix)
 */
fun CustomBooleanFunction(
        functionName: String, postfix: String = "", vararg params: Expression<*>
): CustomFunction<Boolean?> =
        object : CustomFunction<Boolean?>(functionName, BooleanColumnType(), *params) {
            override fun toQueryBuilder(queryBuilder: QueryBuilder) {
                super.toQueryBuilder(queryBuilder)
                if (postfix.isNotEmpty()) {
                    queryBuilder.append(postfix)
                }
            }
        }
