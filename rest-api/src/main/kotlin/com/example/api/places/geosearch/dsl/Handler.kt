package com.example.api.places.geosearch.dsl

import com.example.api.places.common.db.PlaceRecord
import com.example.api.places.common.db.PlaceTable
import com.example.api.places.common.rest.response.toPlaceDto
import com.example.api.places.geosearch.PlacesGeoSearchRequest
import com.example.api.places.geosearch.PlacesGeoSearchResponse
import com.example.api.places.geosearch.PlacesGeoSearchResponseItem
import com.example.util.exposed.functions.postgres.*
import com.example.util.exposed.functions.postgres.gis.experimental.ll_to_earth_exp
import com.example.util.exposed.query.toSQL
import com.example.util.time.durationToNowInMillis
import mu.KLogging
import org.funktionale.tries.Try
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

private typealias Request = PlacesGeoSearchRequest
private typealias Response = PlacesGeoSearchResponse
private typealias ResponseItem = PlacesGeoSearchResponseItem

@Component
class GeoSearchDslHandler {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun handle(req: Request): Response {
        val startedAt = Instant.now()
        return Try { handleInternal(req) }
                .log(req, startedAt)
                .get()
    }

    private fun handleInternal(req: Request): Response = req
            .let(::search)
            .let(::mapToResponse)

    private fun mapToResponse(source: SearchResult): Response = source.items
            .map { ResponseItem(distance = it.distance, place = it.placeRecord.toPlaceDto()) }
            .let { Response(items = it) }

    private fun search(req: Request): SearchResult {
        val logCtxInfo: String = req.logCtxInfo
        val startedAt = Instant.now()


        val reqEarthExpr = ll_to_earth_exp(
                latitude = req.payload.latitude, longitude =req.payload.longitude
        )

        /*
        val t = object :Table() {
            val lat = decimal("",2,2)
        }

         */
        val dbEarthExpr = ll_to_earth_exp(
                latitude = PLACE.latitude, longitude = PLACE.longitude
        )
        val earthDistanceExpr = earth_distance(
                fromEarth = reqEarthExpr, toEarth = dbEarthExpr
        )

        val reqEarthBoxExpr = earth_box(
                earth = reqEarthExpr,
                earthDistance = intParam(req.payload.radiusInMeter)
        )

        val saddrExpr = PLACE.streetAddress.lowerCase().alias("saddr")
        val otherExpr = PLACE.latitude.max().alias("othr")

        val dbEarthExpr2 = ll_to_earth2(
                latitude = PLACE.latitude, longitude = PLACE.longitude
        )

        val dbEarthExpr3 = LLToEarth(
                latitude = PLACE.latitude, longitude = PLACE.longitude
        )

        val foo = ll_to_earth_exp(5.0,null)
                .alias("ll_to_earthD")


        return PLACE
                .slice(
                        dbEarthExpr,
                        earthDistanceExpr,
                        //saddrExpr,
                        dbEarthExpr2,
                        foo,
                        *PLACE.columns.toTypedArray()
                )
                .select {
                    (PLACE.active eq true)
                            .and(earthDistanceExpr.lessEq(req.payload.radiusInMeter.toDouble()))
                            .and(reqEarthBoxExpr.pgContains(dbEarthExpr))
                            .and(PLACE.latitude.greaterEq(BigDecimal.ZERO))
                }
                .orderBy(
                        Pair(earthDistanceExpr, SortOrder.ASC),
                        Pair(PLACE.createdAt, SortOrder.ASC),
                        Pair(PLACE.place_id, SortOrder.ASC)
                )
                .limit(n = req.payload.limit, offset = req.payload.offset)
                .also {
                    logger.info("SEARCH (dsl): $logCtxInfo - prepare sql: ${it.toSQL()}")
                }
                .map {
                    val row=it

                    val value = row[foo]
                    val value2 = row[dbEarthExpr]
                    val value3 = row[earthDistanceExpr]


                    val placeRecord: PlaceRecord = PLACE.mapRowToRecord(it)
                    val distance: Double = it[earthDistanceExpr]
                    SearchResult.Item(distance = distance, placeRecord = placeRecord)
                }
                .let { SearchResult(items = it) }
                .also {
                    logger.info {
                        "SEARCH (dsl): COMPLETE. $logCtxInfo" +
                                " - duration: ${startedAt.durationToNowInMillis()} ms " +
                                " - result.items.count: ${it.items.size}"
                    }
                }
    }

    private fun Try<Response>.log(req: Request, startedAt: Instant): Try<Response> {
        val logCtxInfo: String = req.logCtxInfo
        this.onFailure {
            logger.error {
                "Handler FAILED! $logCtxInfo" +
                        " - duration: ${startedAt.durationToNowInMillis()} ms" +
                        " - reason: ${it.message}" +
                        " - req: $req"
            }
        }
        this.onSuccess {
            logger.info {
                "Handler Success. $logCtxInfo" +
                        " - duration: ${startedAt.durationToNowInMillis()} ms" +
                        " - response.items.count: ${it.items.size}"
            }
        }

        return this
    }

}

private val PLACE = PlaceTable

private data class SearchResult(
        val items: List<Item>
) {
    data class Item(val distance: Double, val placeRecord: PlaceRecord)
}

private val Request.logCtxInfo: String
    get() = "(logId: $logId)"
