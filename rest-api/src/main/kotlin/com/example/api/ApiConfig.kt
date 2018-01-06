package com.example.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class ApiConfig(
        @Value(value = "\${app.appName}") val appName: String
) {
    val title: String
        get() = "API $appName"
}