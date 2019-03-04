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
        val tweetsTable = TweetsTable
        val filterPredicates: List<Op<Boolean>> = req.filter.toPredicates()
        val matchPredicates: List<Op<Boolean>> = req.match.toPredicates()
        val orderByExpressions: List<SortExpression> =
                (req.orderBy.toExpressions()) + Pair(tweetsTable.id, SortOrder.ASC)

        val startedAt: Instant = Instant.now()
        return tweetsTable
                .slice(tweetsTable.columns)
                .select {
                    Op.TRUE and
                            when (filterPredicates.isEmpty()) {
                                true -> Op.TRUE
                                false -> filterPredicates.compoundAnd()
                            } and
                            when (matchPredicates.isEmpty()) {
                                true -> Op.TRUE
                                false -> matchPredicates.compoundOr()
                            }
                }
                .limit(n = req.limit, offset = req.offset)
                .orderBy(*(orderByExpressions.toTypedArray()))
                .map { with(TweetsTable) { it.toTweetsRecord() } }
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

private typealias SortExpression = Pair<Column<*>, SortOrder>

private fun Set<TweeterSearchRequest.OrderBy>.toExpressions(): List<SortExpression> =
        map { Pair(it.field, it.sortOrder) }

private fun TweeterSearchRequest.Match.toPredicates(): List<Op<Boolean>> {
    val tweetsTable = TweetsTable
    val match = this
    return listOfNotNull(
            when (match.messageLIKE.isNullOrEmpty()) {
                true -> null
                false -> (tweetsTable.message.lowerCase() like "%${match.messageLIKE.toLowerCase()}%")
            },
            when (match.commentLIKE.isNullOrEmpty()) {
                true -> null
                false -> (tweetsTable.comment.lowerCase() like "%${match.commentLIKE.toLowerCase()}%")
            }
    )
}

private fun TweeterSearchRequest.Filter.toPredicates(): List<Op<Boolean>> {
    val tweetsTable = TweetsTable
    val filter = this
    return listOfNotNull(
            when (filter.idIN.isNullOrEmpty()) {
                true -> null
                false -> (tweetsTable.id inList filter.idIN)
            },
            when (filter.statusIN.isNullOrEmpty()) {
                true -> null
                false -> (tweetsTable.status inList filter.statusIN)
            },
            when (val value: Instant? = filter.createdAtGTE) {
                null -> null
                else -> (tweetsTable.createdAt greaterEq value)
            },
            when (val value: Instant? = filter.createdAtLOE) {
                null -> null
                else -> (tweetsTable.createdAt lessEq value)
            },
            when (val value: Instant? = filter.modifiedAtGTE) {
                null -> null
                else -> (tweetsTable.modifiedAt greaterEq value)
            },
            when (val value: Instant? = filter.modifiedAtLOE) {
                null -> null
                else -> (tweetsTable.modifiedAt lessEq value)
            },
            when (val value: Int? = filter.versionEQ) {
                null -> null
                else -> (tweetsTable.version eq value)
            },
            when (val value: Int? = filter.versionGTE) {
                null -> null
                else -> (tweetsTable.version greaterEq value)
            },
            when (val value: Int? = filter.versionLOE) {
                null -> null
                else -> (tweetsTable.version lessEq value)
            }
    )
}