package com.example.util.exposed.query

import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.QueryBuilder

fun Query.toSQL():String = prepareSQL(QueryBuilder(false))
