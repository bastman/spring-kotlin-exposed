package com.example.api.tweeter.search

import com.example.api.tweeter.db.TweetStatus
import com.example.api.tweeter.db.TweetsRecord
import com.example.api.tweeter.db.TweetsRepo
import com.example.api.tweeter.db.TweetsTable
import com.example.api.tweeter.toTweetsDto
import com.example.config.Jackson
import com.example.testutils.json.shouldEqualJson
import com.example.testutils.json.toJson
import com.example.testutils.spring.BootWebMockMvcTest
import com.example.util.resources.loadResource
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KLogging
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*

private typealias Request = TweeterSearchRequest
private typealias Response = TweeterSearchResponse

class TweetsRepoTest(
        @Autowired private val repo: TweetsRepo,
        @Autowired private val search: TweeterSearchHandler
) : BootWebMockMvcTest() {
    companion object : KLogging(), GenerateTestCaseTrait

    private val given: GoldenTestData = loadResource("/tests/api/tweeter/search/golden-test-data.json")
            .let { JSON.readValue(it) }

    @Test
    fun `search should work`() {
        saveGoldenTestDataIntoDb(goldenData = given)

        loadTestCase("001")
                .also { testCase ->
                    val responseExpected: Response = testCase.response
                    val responseGiven: Response = search.handle(testCase.request)
                    responseGiven.toJson() shouldEqualJson responseExpected.toJson()
                }
    }

    private fun saveGoldenTestDataIntoDb(goldenData: GoldenTestData) {
        goldenData.items.forEach { repo.insert(it) }
        repo.findAll().sortedBy { it.id } shouldEqual given.items.sortedBy { it.id }
    }

    private fun loadTestCase(name: String): TestCase =
            loadResource("/tests/api/tweeter/search/testcase-$name.json")
                    .let { JSON.readValue(it) }
}

private val JSON = Jackson.defaultMapper()

private data class GoldenTestData(val items: List<TweetsRecord>)
private data class TestCase(val request: Request, val response: Response)

interface GenerateTestCaseTrait {
    fun generateGoldenTestData(repo: TweetsRepo, maxRecords: Int): Any {
        val words: List<String> = "The quick brown fox jumps over the lazy dog".split(" ")
        val records: List<TweetsRecord> = (0..maxRecords).map {
            val now: Instant = Instant.now()
            TweetsRecord(
                    id = UUID.randomUUID(),
                    createdAt = now,
                    modifiedAt = now,
                    deletedAt = Instant.EPOCH,
                    status = TweetStatus.values().random(),
                    comment = "comment: ${words.shuffled().take(3).joinToString(separator = " ")}",
                    message = "message: ${words.shuffled().take(5).joinToString(separator = " ")}",
                    version = (0..10).random()

            ).let(repo::insert)
        }
        return mapOf(
                "items" to records.map { with(TweetsTable) { it.toTweetsDto() } }
        )
    }

    fun generateTestCase(handler: TweeterSearchHandler) {
        val reqJson = """
{
  "limit": 10,
  "offset": 0,
  "match": {
    "message-LIKE": "fox",
    "comment-LIKE": "brown"
  },
  "filter": {
    "status-IN": [
      "DRAFT",
      "PUBLISHED"
    ]
  },
  "orderBy": [
    "createdAt-DESC"
  ]
}
 """.trimIndent()

        val req: Request = JSON.readValue(reqJson)

        println("======= req =======")
        println(JSON.writeValueAsString(req))
        println("===================")

        val resp = handler.handle(req)
        println("======= resp =======")
        println(JSON.writeValueAsString(resp))
        println("===================")

        val t = TestCase(request = req, response = resp)
        println("======= testcase =======")
        println(JSON.writeValueAsString(t))
        println("===================")

    }
}