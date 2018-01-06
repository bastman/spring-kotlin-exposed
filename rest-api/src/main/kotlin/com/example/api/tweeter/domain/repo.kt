package com.example.api.tweeter.domain


import com.example.api.tweeter.domain.db.Tweets
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.joda.time.DateTime
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
        Tweets.insert(toRow(tweet))
        return tweet
    }

    fun update(tweet: Tweet): Tweet {
        Tweets.update({ Tweets.id eq tweet.id }) {
            //it[id] = tweet.id
            it[createdAt] = DateTime.now() //tweet.createdAt.toJodaDateTime()
            it[modifiedAt] = DateTime.now() //tweet.modifiedAt.toJodaDateTime()
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


    private fun toRow(tweet: Tweet): Tweets.(UpdateBuilder<*>) -> Unit = {
        it[id] = tweet.id
        it[createdAt] = DateTime.now() //tweet.createdAt.toJodaDateTime()
        it[modifiedAt] = DateTime.now()//tweet.modifiedAt.toJodaDateTime()
        it[version] = tweet.version
        it[message] = tweet.message
        it[comment] = tweet.comment
    }

    private fun fromRow(r: ResultRow) =
            Tweet(
                    id = r[Tweets.id],
                    createdAt = r[Tweets.createdAt].toInstantJava(),
                    modifiedAt = r[Tweets.modifiedAt].toInstantJava(),
                    version = r[Tweets.version],
                    message = r[Tweets.message],
                    comment = r[Tweets.comment]
            )

}

private fun DateTime.toInstantJava() = Instant.ofEpochMilli(this.millis)
private fun Instant.toJodaDateTime() = DateTime(this)