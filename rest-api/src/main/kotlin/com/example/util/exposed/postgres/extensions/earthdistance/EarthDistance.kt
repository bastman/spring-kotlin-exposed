package com.example.util.exposed.postgres.extensions.earthdistance

import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.DoubleColumnType

// select earth() -> returns "Earth as float8" ( SELECT '6378168'::float8 )
fun earth(): CustomFunction<Double> {
    val fn = CustomFunction<Double>("earth", DoubleColumnType())
    return fn
}
