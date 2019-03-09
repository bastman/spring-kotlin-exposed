package com.example.api.tweeter.search

import com.example.api.tweeter.db.TweetsRecord
import com.example.api.tweeter.db.TweetsTable
import com.example.api.tweeter.toTweetsDto
import com.example.util.time.durationToNowInMillis
import mu.KLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import com.example.api.tweeter.search.TweeterSearchRequest.OrderBy as OrderSpec

private typealias Request = TweeterSearchRequest
private typealias Response = TweeterSearchResponse

@Component
class TweeterSearchHandler {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun handle(req: Request): Response {
        val startedAt: Instant = Instant.now()
        return req
                .let(::query)
                .let(::mapToResponse)
                .also { logSuccess(startedAt) }
    }

    private fun mapToResponse(records: List<TweetsRecord>): Response =
            Response(items = records.map { it.toTweetsDto() })

    private fun query(req: Request): List<TweetsRecord> {
        val filterPredicates: List<Op<Boolean>>? = req.filter?.toPredicates()
        val matchPredicates: List<Op<Boolean>>? = req.match?.toPredicates()
        val orderByExpressions: List<SortExpression> =
                when (val orderBy: Set<OrderSpec>? = req.orderBy) {
                    null -> listOf(Pair(TWEETS.id, SortOrder.ASC))
                    else -> (orderBy.toExpressions()) + Pair(TWEETS.id, SortOrder.ASC)
                }

        val startedAt: Instant = Instant.now()
        return TWEETS
                .slice(TWEETS.columns)
                .select {
                    Op.TRUE and
                            when (filterPredicates.isNullOrEmpty()) {
                                true -> Op.TRUE
                                false -> filterPredicates.compoundAnd()
                            } and
                            when (matchPredicates.isNullOrEmpty()) {
                                true -> Op.TRUE
                                false -> matchPredicates.compoundOr()
                            }
                }
                .limit(n = req.limit, offset = req.offset)
                .orderBy(*(orderByExpressions.toTypedArray()))
                .map { with(TWEETS) { it.toTweetsRecord() } }
                .also {
                    logger.info {
                        "Found ${it.size} items in db." +
                                " duration: ${startedAt.durationToNowInMillis()} ms." +
                                " req: $req"
                    }
                }
    }

    private fun logSuccess(startedAt: Instant) =
            logger.info { "Handler Success. duration: ${startedAt.durationToNowInMillis()} ms" }

}

private val TWEETS = TweetsTable

private typealias SortExpression = Pair<Column<*>, SortOrder>

private fun Set<TweeterSearchRequest.OrderBy>.toExpressions(): List<SortExpression> =
        map { Pair(it.field, it.sortOrder) }

private fun TweeterSearchRequest.Match.toPredicates(): List<Op<Boolean>> {
    val match = this
    return listOfNotNull(
            when (match.messageLIKE.isNullOrEmpty()) {
                true -> null
                false -> (TWEETS.message.lowerCase() like "%${match.messageLIKE.toLowerCase()}%")
            },
            when (match.commentLIKE.isNullOrEmpty()) {
                true -> null
                false -> (TWEETS.comment.lowerCase() like "%${match.commentLIKE.toLowerCase()}%")
            }
    )
}

private fun TweeterSearchRequest.Filter.toPredicates(): List<Op<Boolean>> {
    val filter = this
    return listOfNotNull(
            when (filter.idIN.isNullOrEmpty()) {
                true -> null
                false -> (TWEETS.id inList filter.idIN)
            },
            when (filter.statusIN.isNullOrEmpty()) {
                true -> null
                false -> (TWEETS.status inList filter.statusIN)
            },
            when (val value: Instant? = filter.createdAtGTE) {
                null -> null
                else -> (TWEETS.createdAt greaterEq value)
            },
            when (val value: Instant? = filter.createdAtLOE) {
                null -> null
                else -> (TWEETS.createdAt lessEq value)
            },
            when (val value: Instant? = filter.modifiedAtGTE) {
                null -> null
                else -> (TWEETS.modifiedAt greaterEq value)
            },
            when (val value: Instant? = filter.modifiedAtLOE) {
                null -> null
                else -> (TWEETS.modifiedAt lessEq value)
            },
            when (val value: Int? = filter.versionEQ) {
                null -> null
                else -> (TWEETS.version eq value)
            },
            when (val value: Int? = filter.versionGTE) {
                null -> null
                else -> (TWEETS.version greaterEq value)
            },
            when (val value: Int? = filter.versionLOE) {
                null -> null
                else -> (TWEETS.version lessEq value)
            }
    )
}