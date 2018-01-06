package com.example.config

import com.example.api.ApiConfig
import com.google.common.base.Predicates
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class Swagger(private val apiConfig: ApiConfig) {

    @Bean
    fun mainApi(): Docket
            = apiConfig.toDocket()
            .groupName("Main")
            .select()
            .apis(RequestHandlerSelectors.basePackage(apiConfig.getBasePackageName()))
            .build()

    @Bean
    fun monitoringApi(): Docket
            = apiConfig.toDocket()
            .groupName("Monitoring")
            .useDefaultResponseMessages(true)
            .select()
            .apis(Predicates.not(RequestHandlerSelectors.basePackage(apiConfig.getBasePackageName())))
            .build()
}

private fun ApiConfig.getBasePackageName() = this::class.java.`package`.name
private fun ApiConfig.toApiInfo() = springfox.documentation.builders.ApiInfoBuilder().title(this.title).build()
private fun ApiConfig.toDocket() = springfox.documentation.spring.web.plugins.Docket(springfox.documentation.spi.DocumentationType.SWAGGER_2).apiInfo(this.toApiInfo())