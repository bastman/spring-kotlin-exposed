package com.example.api.tweeter.search

import com.example.api.tweeter.db.TweetStatus
import com.example.api.tweeter.db.TweetsTable
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import io.swagger.annotations.ApiModel
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SortOrder
import java.time.Instant
import java.util.*

private const val SWAGGER_API_MODEL_PREFIX = "TweeterSearchRequest"

@ApiModel(SWAGGER_API_MODEL_PREFIX)
data class TweeterSearchRequest(
        val limit: Int,
        val offset: Int,
        val match: Match?,
        val filter: Filter?,
        val orderBy: List<OrderBy>?, // Set<OrderBy> may lead to inconsistent order within the set
        val jq:String?
) {
    @ApiModel("${SWAGGER_API_MODEL_PREFIX}_Payload_OrderBy")
    enum class OrderBy(@get:JsonValue val jsonValue: String, val field: Column<*>, val sortOrder: SortOrder) {
        CREATED_AT_DESC("createdAt-DESC", TweetsTable.createdAt, SortOrder.DESC),
        CREATED_AT_ASC("createdAt-ASC", TweetsTable.createdAt, SortOrder.ASC),
        MODIFIED_AT_DESC("modifiedAt-DESC", TweetsTable.modifiedAt, SortOrder.DESC),
        MODIFIED_AT_ASC("modifiedAt-ASC", TweetsTable.modifiedAt, SortOrder.ASC),
        VERSION_DESC("version-DESC", TweetsTable.version, SortOrder.DESC),
        VERSION_ASC("version-ASC", TweetsTable.version, SortOrder.ASC),
        ID_DESC("id-DESC", TweetsTable.id, SortOrder.DESC),
        ID_ASC("id-ASC", TweetsTable.id, SortOrder.ASC),
        ;
    }

    @ApiModel("${SWAGGER_API_MODEL_PREFIX}_Payload_Filter")
    data class Filter(
            // SQL: AND, e.g: ( id IN ("123","456") AND status IN(DRAFT, PENDING)   )
            @JsonProperty("id-IN") val idIN: Set<UUID>?,
            @JsonProperty("status-IN") val statusIN: Set<TweetStatus>?,
            @JsonProperty("createdAt-GTE") val createdAtGTE: Instant?,
            @JsonProperty("createdAt-LOE") val createdAtLOE: Instant?,
            @JsonProperty("modifiedAt-GTE") val modifiedAtGTE: Instant?,
            @JsonProperty("modifiedAt-LOE") val modifiedAtLOE: Instant?,
            @JsonProperty("version-GTE") val versionGTE: Int?,
            @JsonProperty("version-LOE") val versionLOE: Int?,
            @JsonProperty("version-EQ") val versionEQ: Int?
    )

    @ApiModel("${SWAGGER_API_MODEL_PREFIX}_Payload_Match")
    data class Match(
            // SQL: OR, e.g: (  (message LIKE "%foo%) OR (comment LIKE "%bar%)  )
            @JsonProperty("message-LIKE") val messageLIKE: String?,
            @JsonProperty("comment-LIKE") val commentLIKE: String?
    )
}
