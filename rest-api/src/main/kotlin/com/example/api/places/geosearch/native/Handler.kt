package com.example.api.places.geosearch.native

import com.example.api.places.common.db.PlaceRecord
import com.example.api.places.common.db.PlaceRepo
import com.example.api.places.common.rest.response.toPlaceDto
import com.example.api.places.geosearch.PlacesGeoSearchRequest
import com.example.api.places.geosearch.PlacesGeoSearchResponse
import com.example.api.places.geosearch.PlacesGeoSearchResponseItem
import com.example.api.places.geosearch.native.service.GeoSearchServiceRequest
import com.example.api.places.geosearch.native.service.GeoSearchServiceResult
import com.example.api.places.geosearch.native.service.PlacesGeoSearchService
import com.example.util.time.durationToNowInMillis
import mu.KLogging
import org.funktionale.tries.Try
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

private typealias Request = PlacesGeoSearchRequest
private typealias Response = PlacesGeoSearchResponse
private typealias ResponseItem = PlacesGeoSearchResponseItem

@Component
class GeoSearchNativeHandler(
        private val placeRepo: PlaceRepo,
        private val geoSearchService: PlacesGeoSearchService
) {
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
            .let(::joinPropertyRecords)
            .let(::mapToResponse)

    private fun mapToResponse(source: JoinResult): Response = Response(items = source.items)

    private fun search(req: Request): ServiceResult {
        val logCtxInfo: String = req.logCtxInfo
        val startedAt = Instant.now()
        val searchReq = ServiceRequest(
                latitude = req.payload.latitude,
                longitude = req.payload.longitude,
                radiusInMeter = req.payload.radiusInMeter,
                limit = req.payload.limit,
                offset = req.payload.offset
        )

        return geoSearchService
                .find(searchReq)
                .also {
                    logger.info {
                        "SEARCH (native): COMPLETE. $logCtxInfo" +
                                " - duration: ${startedAt.durationToNowInMillis()} ms " +
                                " - result.items.count: ${it.items.size}"
                    }
                }
    }

    private fun joinPropertyRecords(source: ServiceResult): JoinResult {
        val sourceItems: List<ServiceResultItem> = source.items
        val placeIds: Set<UUID> = sourceItems.map { it.placeId }.distinct().toSet()
        if (placeIds.isEmpty()) {
            return JoinResult.EMPTY
        }
        val placeRecordsById: Map<UUID, PlaceRecord> = placeRepo
                .findByIdList(placeIds = placeIds, isActive = null)
                .associateBy { it.place_id }
        val sinkItems: List<ResponseItem> = sourceItems
                .mapNotNull { geoSearchItem: ServiceResultItem ->
                    when (val placeRecord: PlaceRecord? = placeRecordsById[geoSearchItem.placeId]) {
                        null -> null
                        else -> ResponseItem(
                                place = placeRecord.toPlaceDto(),
                                distance = geoSearchItem.distance
                        )
                    }
                }
        return JoinResult(items = sinkItems)
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

private typealias ServiceRequest = GeoSearchServiceRequest
private typealias ServiceResult = GeoSearchServiceResult
private typealias ServiceResultItem = GeoSearchServiceResult.Item

private val Request.logCtxInfo: String
    get() = "(logId: $logId)"

private data class JoinResult(val items: List<ResponseItem>) {
    companion object {
        val EMPTY = JoinResult(items = emptyList())
    }
}
