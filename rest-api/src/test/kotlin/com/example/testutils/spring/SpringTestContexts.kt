package com.example.testutils.spring

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles(SpringProfiles.TEST)
@Transactional
@TestPropertySource(properties = ["foo=foo1"])
//@AutoConfigureMockMvc // NEW !!! lets see
@AutoConfigureJdbc // lets see
@ImportAutoConfiguration
//@EnableAspectJAutoProxy
abstract class BootWebMockMvcTest


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(SpringProfiles.TEST)
@Transactional
@TestPropertySource(properties = ["foo=foo3"])
@ImportAutoConfiguration
@AutoConfigureJdbc
//@EnableAspectJAutoProxy
abstract class BootWebRandomPortTest