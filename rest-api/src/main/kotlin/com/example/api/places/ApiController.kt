package com.example.api.places

import com.example.api.places.common.db.PlaceRepo
import com.example.api.places.common.db.PlaceTable
import com.example.api.places.common.rest.mutation.Mutations
import com.example.api.places.common.rest.mutation.toRecord
import com.example.api.places.common.rest.response.ListResponseDto
import com.example.api.places.common.rest.response.PlaceDto
import com.example.api.places.common.rest.response.toPlaceDto
import com.example.api.places.geosearch.PlacesGeoSearchRequest
import com.example.api.places.geosearch.PlacesGeoSearchResponse
import com.example.api.places.geosearch.dsl.GeoSearchDslHandler
import com.example.api.places.geosearch.native.GeoSearchNativeHandler
import com.example.util.exposed.postgres.extensions.earthdistance.*
import com.example.util.exposed.query.toSQL
import mu.KLogging
import org.jetbrains.exposed.sql.ExpressionAlias
import org.jetbrains.exposed.sql.intParam
import org.jetbrains.exposed.sql.selectAll
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

private const val API_BASE_URI = "/api/places"

@RestController
class PlacesApiController(
        private val repo: PlaceRepo,
        private val geoSearchNative: GeoSearchNativeHandler,
        private val geoSearchDsl: GeoSearchDslHandler
) {
    companion object : KLogging()

    @GetMapping(API_BASE_URI)
    @Transactional(readOnly = true)
    fun findAll(): ListResponseDto<PlaceDto> = repo
            .findAll(isActive = true)
            .map { it.toPlaceDto() }
            .let { ListResponseDto(items = it) }

    @PutMapping(API_BASE_URI)
    @Transactional(readOnly = false)
    fun create(@RequestBody req: Mutations.CreatePlace): PlaceDto = req
            .toRecord(placeId = UUID.randomUUID(), now = Instant.now())
            .let(repo::insert)
            .also { logger.info { "INSERT DB ENTITY: $it" } }
            .toPlaceDto()

    @DeleteMapping("$API_BASE_URI/{placeId}")
    @Transactional(readOnly = false)
    fun softDelete(@PathVariable("placeId") placeId: UUID): PlaceDto = repo
            .softDeleteById(placeId = placeId, deletedAt = Instant.now())
            .also { logger.info { "SOFT DELETE DB ENTITY: $it" } }
            .toPlaceDto()

    @PostMapping("$API_BASE_URI/{placeId}/restore")
    @Transactional(readOnly = false)
    fun softRestore(@PathVariable("placeId") placeId: UUID): PlaceDto = repo
            .softRestoreById(placeId = placeId)
            .also { logger.info { "SOFT RESTORE DB ENTITY: $it" } }
            .toPlaceDto()

    @PostMapping("$API_BASE_URI/geosearch/native")
    fun geoSearchNative(@RequestBody payload: PlacesGeoSearchRequest.Payload): PlacesGeoSearchResponse =
            PlacesGeoSearchRequest(payload = payload)
                    .let(geoSearchNative::handle)

    @PostMapping("$API_BASE_URI/geosearch/dsl")
    @Transactional(readOnly = true)
    fun geoSearchDsl(@RequestBody payload: PlacesGeoSearchRequest.Payload): PlacesGeoSearchResponse =
            PlacesGeoSearchRequest(payload = payload)
                    .let(geoSearchDsl::handle)

    @PostMapping("$API_BASE_URI/foo")
    @Transactional(readOnly = true)
    fun foo() {

        val earth_expr = earth()
        val ll_expr_nullable=ll_to_earth(1.0,null)
        val ll_expr_not_nullable=ll_to_earth(1.0,2.0)
        val ll_expr2_not_nullable=ll_to_earth(2.0,3.0)
        val req_earth = PGEarthPointLocation(
                5881394.65979286, 2140652.5921368, 1227937.44619261
        )
        val ll_to_earth_col_expr_not_nullable = ll_to_earth(
                PlaceTable.latitude, PlaceTable.longitude
        )

        //val lat_expr = req_earth.latitude4().nn()//.nullable()
        val lat_expr = latitude(req_earth)
        val isNuallbale = lat_expr.columnType.nullable
        val lat_expr_alias = ExpressionAlias(lat_expr, "the_lat")
        //val req_earth = PGEarthPointLocation

        val earth_distance_expr = earth_distance(ll_expr_not_nullable,ll_expr2_not_nullable)
        val box_expr = earth_box(
                fromLocation = ll_to_earth(2.0,3.0),
                greatCircleRadiusInMeter = intParam(100)
        )

        val query = PlaceTable.slice(
                earth_expr,ll_expr_nullable,ll_expr_not_nullable, lat_expr_alias,
                ll_to_earth_col_expr_not_nullable,earth_distance_expr,box_expr)
                .selectAll()
                .limit(1)
                .also {
                    println("SQL: ${it.toSQL()}")
                }

                query
                .map {
                    val row=it
                   // val earth = it[earth_expr]
                   // val ll=it[ll_expr]
                   val lat = it[lat_expr_alias]
                    val r_ll_expr_nullable = it[ll_expr_nullable]
                    val r_ll_expr_not_nullable = it[ll_expr_not_nullable]
                    val r_ll_to_earth_col_expr_not_nullable = it[ll_to_earth_col_expr_not_nullable]
                    val r_earth_distance_expr=it[earth_distance_expr]
                    var r_box_expr=it[box_expr]
                    "foo"
                }

    }
}




