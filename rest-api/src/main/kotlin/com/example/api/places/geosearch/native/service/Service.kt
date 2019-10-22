package com.example.api.places.geosearch.native.service

import com.akelius.offerb2b.bff.api.property.geosearch.service.GeoSearchServiceRequest
import com.akelius.offerb2b.bff.api.property.geosearch.service.GeoSearchServiceResult
import com.example.api.places.common.db.PlaceTable
import com.example.util.exposed.nativesql.INativeSql
import mu.KLogging
import org.funktionale.tries.Try
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

private typealias Request = GeoSearchServiceRequest
private typealias Result = GeoSearchServiceResult
private typealias ResultItem = GeoSearchServiceResult.Item

@Component
class PlacesGeoSearchService {
    companion object : KLogging(), INativeSql

    // NOTE: uses index ... CREATE INDEX place_geosearch_index ON property USING gist (ll_to_earth(latitude, longitude));
    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    fun find(req: Request): Result {
        val selectFields = listOf(
                PLACE.place_id.qName
        )

        val sql: String = """
                    SELECT
                        ${selectFields.joinToString(" , ")},

                        earth_distance(
                            ll_to_earth( ${req.latitude} , ${req.longitude} ),
                            ll_to_earth( ${PLACE.latitude.qName}, ${PLACE.longitude.qName} )
                        ) as $FIELD_DISTANCE

                    FROM
                        ${PLACE.qTableName}

                    WHERE
                        earth_box(
                            ll_to_earth( ${req.latitude} , ${req.longitude} ), ${req.radiusInMeter}
                        ) @> ll_to_earth( ${PLACE.latitude.qName} , ${PLACE.longitude.qName} )

                        AND
                            earth_distance(
                                ll_to_earth( ${req.latitude} , ${req.longitude} ),
                                ll_to_earth( ${PLACE.latitude.qName}, ${PLACE.longitude.qName} )
                            ) <= ${req.radiusInMeter}

                    ORDER BY
                        $FIELD_DISTANCE ASC,
                        ${PLACE.createdAt.qName} ASC,
                        ${PLACE.place_id.qName} ASC

                    LIMIT ${req.limit}
                    OFFSET ${req.offset}

                    ;
        """.trimIndent()

        val items: List<ResultItem> = Try { execSql(sql = sql) }
                .onFailure {
                    logger.error { "SQL QUERY FAILED ! error.message: ${it.message} - req: $req - SQL: $sql" }
                }
                .get()

        return Result(items = items)
                .also {
                    logger.info { "==== result.items.count: ${it.items.size} - req: $req - SQL: $sql" }
                }
    }

    private fun execSql(sql: String): List<ResultItem> =
            sqlExecAndMap(sql = sql, transaction = TransactionManager.current()) {
                val meta: Map<String, Int> = it.metaData.toQualifiedColumnIndexMap()
                ResultItem(
                        placeId = UUID.fromString(it.getString(meta.getValue(PLACE.place_id.qName))),
                        distance = it.getDouble(meta.getValue(FIELD_DISTANCE))
                )
            }

}

private fun sqlExprInIdList(fieldName: String, ids: Set<UUID>): String {
    // select * from place where place.place_id in ('b682f087-5b6d-4d09-bda0-7846a287cb22')
    val valuesExpr: String = ids.map {
        "'$it'"
    }.joinToString(separator = " , ")

    return "$fieldName IN ( $valuesExpr )"

}

private val PLACE = PlaceTable
private val FIELD_DISTANCE = "distance_from_current_location"
