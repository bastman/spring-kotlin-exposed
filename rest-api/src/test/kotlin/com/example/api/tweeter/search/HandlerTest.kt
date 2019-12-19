package com.example.api.tweeter.search

import com.example.api.tweeter.db.TweetStatus
import com.example.api.tweeter.db.TweetsRecord
import com.example.api.tweeter.db.TweetsRepo
import com.example.api.tweeter.db.TweetsTable
import com.example.api.tweeter.toTweetsDto
import com.example.config.Jackson
import com.example.testconfig.CodeSourceResourceBuckets
import com.example.testutils.json.shouldEqualJson
import com.example.testutils.json.toJson
import com.example.testutils.minutest.minuTestFactory
import com.example.testutils.resources.CodeSourceResourceBucket
import com.example.testutils.resources.loadResourceText
import com.example.testutils.spring.BootWebMockMvcTest
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KLogging
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.TestFactory
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

    private val resourceBucket: CodeSourceResourceBucket = CodeSourceResourceBuckets
            .GOLDEN_TEST_DATA
            .bucket
            .withQualifiedName { "${it.qualifiedName}/tests/api/tweeter/search" }

    private val goldenTestDataGiven: GoldenTestData = resourceBucket
            .withQualifiedName { "${it.qualifiedName}/golden-test-data.json" }
            .loadResourceText()
            .let { JSON.readValue(it) }

    private fun loadTestCase(name: String): TestCase = resourceBucket
            .withQualifiedName { "${it.qualifiedName}/testcase-$name.json" }
            .loadResourceText()
            .let { JSON.readValue(it) }

    @TestFactory
    fun `search should work`() = minuTestFactory(
            name = "search should work"
    ) {
        saveGoldenTestDataIntoDb(goldenData = goldenTestDataGiven)
        (1..10).take(10)
                .map { "$it".padStart(3, '0') }
                .forEach { testCaseName ->
                    val testCase = loadTestCase(testCaseName)
                    test(name = "$testCaseName - req: ${testCase.request}") {
                        val req = testCase.request
                        println("req: $req")
                        println("req (json): ${req.toJson()}")
                        val responseExpected: Response = testCase.response
                        println("req.orderBy: ${testCase.request.orderBy}")
                        val responseGiven: Response = search.handle(testCase.request)

                        println("ids expected: ${responseExpected.items.map { it.id }}")
                        println("ids given   : ${responseGiven.items.map { it.id }}")

                        testCase.dump("response expected: $testCaseName")
                        testCase
                                .copy(response = responseGiven)
                                .dump("response given: $testCaseName")

                        responseGiven.toJson() shouldEqualJson responseExpected.toJson()
                    }
                }
    }

    private fun saveGoldenTestDataIntoDb(goldenData: GoldenTestData) {
        goldenData.items.forEach { repo.insert(it) }
        repo.findAll().sortedBy { it.id } shouldEqual goldenTestDataGiven.items.sortedBy { it.id }
    }

}

private val JSON = Jackson.defaultMapper()

private data class GoldenTestData(val items: List<TweetsRecord>)
private data class TestCase(val request: Request, val response: Response)

private fun TestCase.dump(testCaseName: String) {
    println("==== tc: $testCaseName ====")
    println(this.toJson())
    println("================")
}

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

