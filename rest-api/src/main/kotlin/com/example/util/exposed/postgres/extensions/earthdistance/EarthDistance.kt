package com.example.util.exposed.postgres.extensions.earthdistance

import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.DoubleColumnType

// select earth() -> returns the assumed radius of te earth as float8 ( SELECT '6378168'::float8 )
fun earth(): CustomFunction<Double> {
    val fn = CustomFunction<Double>("earth", DoubleColumnType())
    return fn
}


/**

select ll_to_earth( 11.1 , 20.0 ); -> returns (5881394.65979286, 2140652.5921368, 1227937.44619261)
select latitude('(5881394.65979286, 2140652.5921368, 1227937.44619261)'::earth); -> returns 11.1
 */
