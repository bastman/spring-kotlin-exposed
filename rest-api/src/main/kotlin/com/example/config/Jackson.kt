package com.example.config

import com.example.api.common.rest.serialization.Patchable
import com.example.api.common.rest.serialization.PatchableDeserializer
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class Jackson {

    @Bean
    fun objectMapper(): ObjectMapper = defaultMapper()

    companion object {
        fun defaultMapper(): ObjectMapper = jacksonObjectMapper()
                .registerModule(
                        SimpleModule()
                                .addDeserializer(Patchable::class.java, PatchableDeserializer())
                        //  .addSerializer(Patchable::class.java, PatchableSerializer::class)
                )
                //.registerModule(Jdk8Module())
                //.registerModule(JavaTimeModule())
                .findAndRegisterModules()

                // toJson()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)

                // fromJson()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                .disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                .also {
                    println("==> JACKON MODULES: ${it.registeredModuleIds}")
                }
    }
}
