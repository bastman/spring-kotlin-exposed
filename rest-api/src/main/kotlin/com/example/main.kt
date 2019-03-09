package com.example

import org.springframework.boot.Banner
import org.springframework.boot.runApplication

fun main(args: Array<String>) {
    runApplication<RestApiApplication>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}