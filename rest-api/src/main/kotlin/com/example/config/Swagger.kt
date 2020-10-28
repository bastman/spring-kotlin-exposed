package com.example.config

import com.example.api.ApiConfig
import com.example.api.common.rest.serialization.Patchable
import com.fasterxml.classmate.TypeResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spring.web.plugins.Docket
//import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.*

@Configuration
//@EnableSwagger2
class Swagger(private val apiConfig: ApiConfig, private val typeResolver: TypeResolver) {
    // see: https://medium.com/@hala3k/setting-up-swagger-3-with-spring-boot-2-a7c1c3151545

    // http://localhost:8080/v2/api-docs
    // http://localhost:8080/swagger-ui/index.html

    @Bean
    fun mainApi(): Docket = apiConfig.toDocket()
            //.groupName("Main")
            .select()
            .apis(RequestHandlerSelectors.basePackage(apiConfig.getBasePackageName()))
            .build()
            //.additionalModels(typeResolver.resolve(Patchable::class.java, WildcardType::class.java))
            // see: https://github.com/swagger-api/swagger-codegen/issues/7601
            .genericModelSubstitutes(Optional::class.java)
            .genericModelSubstitutes(Patchable::class.java)

}

private fun ApiConfig.getBasePackageName() = this::class.java.`package`.name
private fun ApiConfig.toApiInfo() = springfox.documentation.builders.ApiInfoBuilder().title(this.title).build()
private fun ApiConfig.toDocket() = Docket(springfox.documentation.spi.DocumentationType.SWAGGER_2).apiInfo(this.toApiInfo())
