package com.example.api.tweeter.domain

import com.example.api.common.EntityNotFoundException
import com.example.api.tweeter.domain.db.Tweets
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

data class Tweet(
        val id: UUID,
        val createdAt: Instant,
        val modifiedAt: Instant,
        val version: Int,
        val message: String,
        val comment: String?
)

@Repository
@Transactional // Should be at @Service level in real applications
class TweetRepository {

    fun insert(tweet: Tweet): Tweet {
        Tweets.insert({
            it[id] = tweet.id
            it[createdAt] = tweet.createdAt
            it[modifiedAt] = tweet.modifiedAt
            it[version] = tweet.version
            it[message] = tweet.message
            it[comment] = tweet.comment
        })
        return tweet
    }

    fun update(tweet: Tweet): Tweet {
        Tweets.update({ Tweets.id eq tweet.id }) {
            //it[id] = tweet.id
            it[createdAt] = tweet.createdAt
            it[modifiedAt] = tweet.modifiedAt
            it[version] = tweet.version
            it[message] = tweet.message
            it[comment] = tweet.comment
        }
        return tweet
    }

    fun findAll() = Tweets.selectAll().map { fromRow(it) }

    fun getOneById(id: UUID): Tweet? {
        val foo = Tweets.select { Tweets.id eq id }
                .limit(1)
                .map { fromRow(it) }
                .firstOrNull()

        return foo
    }

    fun requireOneById(id: UUID): Tweet
            = getOneById(id) ?: throw EntityNotFoundException("TweetRecord NOT FOUND ! (id=$id)")


    private fun fromRow(r: ResultRow) =
            Tweet(
                    id = r[Tweets.id],
                    createdAt = r[Tweets.createdAt],
                    modifiedAt = r[Tweets.modifiedAt],
                    version = r[Tweets.version],
                    message = r[Tweets.message],
                    comment = r[Tweets.comment]
            )

}

