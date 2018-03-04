package com.example.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class ApiConfig(
        @Value(value = "\${app.appName}") val appName: String,
        @Value(value = "\${app.envName}") val envName: String
) {
    val title: String
        get() = "API $appName ($envName)"
}