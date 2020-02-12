package com.example.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvc {

    @Bean
    fun webMvcConfigurer(): WebMvcConfigurer = object : WebMvcConfigurer {
        override fun addViewControllers(registry: ViewControllerRegistry) {
            registry.addViewController("/health").setViewName("forward:/actuator/health")
            registry.addRedirectViewController("/", "/swagger-ui.html")
        }
    }
}
