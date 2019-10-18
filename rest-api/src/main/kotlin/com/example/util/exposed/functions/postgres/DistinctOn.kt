package com.example.util.exposed.functions.postgres

import com.example.util.exposed.functions.common.CustomBooleanFunction
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.Expression

/**
 * SELECT DISTINCT ON (a,b,c) TRUE, a,b,x,y,z FROM table WHERE ...
 * see: https://github.com/JetBrains/Exposed/issues/500
 */
fun distinctOn(vararg expressions: Expression<*>): CustomFunction<Boolean?> = CustomBooleanFunction(
        functionName = "DISTINCT ON",
        postfix = " TRUE",
        params = *expressions
)
