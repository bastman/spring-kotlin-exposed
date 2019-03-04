package com.example.api.tweeter.search

import com.example.api.tweeter.db.TweetsRecord
import com.example.api.tweeter.db.TweetsTable
import com.example.api.tweeter.toTweetsDto
import com.example.util.time.durationToNowInMillis
import mu.KLogging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
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
        val filter = req.filter
        val match = req.match
        val orderBy = req.orderBy
        val orderByExpressions = orderBy
                .map { Pair(it.field, it.sortOrder) } + Pair(tweetsTable.id, SortOrder.ASC)

        val filterPredicates: List<Op<Boolean>> = listOfNotNull(
                when (filter.idIN.isNullOrEmpty()) {
                    true -> null
                    false -> (tweetsTable.id inList filter.idIN)
                },
                when (filter.statusIN.isNullOrEmpty()) {
                    true -> null
                    false -> (tweetsTable.status inList filter.statusIN)
                }
        )

        val matchPredicates = listOfNotNull(
                when (match.messageLIKE.isNullOrEmpty()) {
                    true -> null
                    false -> (tweetsTable.message.lowerCase() like "%${match.messageLIKE.toLowerCase()}%")
                },
                when (match.commentLIKE.isNullOrEmpty()) {
                    true -> null
                    false -> (tweetsTable.comment.lowerCase() like "%${match.commentLIKE.toLowerCase()}%")
                }
        )

        val startedAt: Instant = Instant.now()
        return tweetsTable
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