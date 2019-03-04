package com.example.bootstrap

import com.example.testutils.spring.SpringProfiles
import org.amshove.kluent.`should be greater than`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(SpringProfiles.TEST)
@Transactional
class BootstrapIT(
        @LocalServerPort private val port: Int,
        @Autowired webTestClient: WebTestClient
) {
    private val webClient: WebTestClient = webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(60))
            .build()

    @Test
    fun `context loads`() {
        port `should be greater than` 0
    }

    @Test
    fun `GET api health - should return 200`() {
        webClient

                .get().uri("/health")
                .header("Origin", "http://example.com")
                .exchange().expectStatus().isOk
    }

    @Test
    fun `OPTIONS api health - should return 200`() {
        webClient.options().uri("/health")
                .header("Origin", "http://example.com")
                .exchange().expectStatus().isOk
    }


}