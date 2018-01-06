package com.example

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableTransactionManagement
class RestApiApplication

fun main(args: Array<String>) {
    SpringApplication.run(RestApiApplication::class.java, *args)
}