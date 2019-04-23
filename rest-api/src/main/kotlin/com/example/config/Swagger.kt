package com.example.config

import com.example.api.common.rest.serialization.Patchable
import com.example.api.ApiConfig
import com.fasterxml.classmate.TypeResolver
import com.google.common.base.Predicates
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.*

@Configuration
@EnableSwagger2
class Swagger(private val apiConfig: ApiConfig, private val  typeResolver: TypeResolver) {

    @Bean
    fun mainApi(): Docket = apiConfig.toDocket()
            .groupName("Main")
            .select()
            .apis(RequestHandlerSelectors.basePackage(apiConfig.getBasePackageName()))
            .build()
            //.additionalModels(typeResolver.resolve(Patchable::class.java, WildcardType::class.java))
            // see: https://github.com/swagger-api/swagger-codegen/issues/7601
            .genericModelSubstitutes(Optional::class.java)
            .genericModelSubstitutes(Patchable::class.java)

    @Bean
    fun monitoringApi(): Docket = apiConfig.toDocket()
            .groupName("Monitoring")
            .useDefaultResponseMessages(true)
            .select()
            .apis(Predicates.not(RequestHandlerSelectors.basePackage(apiConfig.getBasePackageName())))
            .build()
}

private fun ApiConfig.getBasePackageName() = this::class.java.`package`.name
private fun ApiConfig.toApiInfo() = springfox.documentation.builders.ApiInfoBuilder().title(this.title).build()
private fun ApiConfig.toDocket() = springfox.documentation.spring.web.plugins.Docket(springfox.documentation.spi.DocumentationType.SWAGGER_2).apiInfo(this.toApiInfo())
