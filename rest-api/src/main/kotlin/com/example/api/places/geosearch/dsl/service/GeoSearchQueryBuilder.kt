package com.example.api.places.geosearch.dsl.service


import com.example.util.exposed.postgres.extensions.earthdistance.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq

data class GeoSearchQuery(
        val sliceDistanceAlias: ExpressionAlias<Double>,
        val whereDistanceLessEqRadius: Op<Boolean>,
        val whereEarthBoxContainsLocation: Op<Boolean>,
        val orderByDistance: CustomFunction<Double>
)

fun buildGeoSearchQuery(
        fromLatitude: Number,
        fromLongitude: Number,
        searchRadiusInMeter: Number,
        toLatitudeColumn: Column<out Number>,
        toLongitudeColumn: Column<out Number>,
        returnDistanceAsAlias: String
): GeoSearchQuery {
    val reqEarthExpr: CustomFunction<PGEarthPointLocation> = ll_to_earth(
            latitude = fromLatitude, longitude = fromLongitude
    )
    val dbEarthExpr: CustomFunction<PGEarthPointLocation> = ll_to_earth(
            latitude = toLatitudeColumn, longitude = toLongitudeColumn
    )
    val earthDistanceExpr: CustomFunction<Double> = earth_distance(
            fromEarth = reqEarthExpr, toEarth = dbEarthExpr
    )
    val earthDistanceExprAlias: ExpressionAlias<Double> = ExpressionAlias(
            earthDistanceExpr, returnDistanceAsAlias
    )
    val reqEarthBoxExpr: CustomFunction<PGEarthBox> = earth_box(
            fromLocation = reqEarthExpr,
            greatCircleRadiusInMeter = intParam(searchRadiusInMeter.toInt())
    )

    return GeoSearchQuery(
            sliceDistanceAlias = earthDistanceExprAlias,
            whereDistanceLessEqRadius = (earthDistanceExpr lessEq searchRadiusInMeter.toDouble()),
            whereEarthBoxContainsLocation = (reqEarthBoxExpr rangeContains dbEarthExpr),
            orderByDistance = earthDistanceExpr
    )
}


