package com.example.config.db

import mu.KLogging
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.time.Instant

enum class FlywayStrategyName { SKIP, VALIDATE, MIGRATE, REPAIR, BASELINE; }

@Component
data class FlywayConfig(
        @Value(value = "\${app.flyway.info}") val info: Boolean,
        @Value(value = "\${app.flyway.strategy}") val strategyName: FlywayStrategyName
)

@Configuration
class FlywayConfiguration(
        private val config: FlywayConfig
) {
    companion object : KLogging()

    @Bean
    fun flywayMigrationStrategy(): FlywayMigrationStrategy =
            FlywayMigrationStrategy { flyway -> executeFlyway(flyway) }

    private fun executeFlyway(flyway: Flyway) {
        logger.info { "==== flyway (START): config=$config ... ====" }
        try {
            if (config.info) {
                logger.info("flyway.info()")
                flyway.info()
            }
            executeStrategy(flyway = flyway)
        } catch (all: Exception) {
            logger.error { " Flyway Failed! strategy: ${config.strategyName} reason: ${all.message}" }
            throw all
        }
        logger.info { "==== flyway (DONE): config: $config ====" }
    }

    private fun executeStrategy(flyway: Flyway) {
        logger.info { "=> execute flyway strategy: ${config.strategyName} ${Instant.now()} ..." }
        return when (config.strategyName) {
            FlywayStrategyName.SKIP -> {
                logger.info("flyway: ${config.strategyName}: Do nothing with flyway.")
            }
            FlywayStrategyName.VALIDATE -> {
                logger.info("=== VALIDATE flyway: ${config.strategyName} ${Instant.now()} ...")
                try {
                    flyway.validate()
                } catch (all: Throwable) {
                    logger.error("flyway FAILED! ${all.message}", all)
                    throw  all
                }
            }
            FlywayStrategyName.MIGRATE -> {
                logger.info("==== MIGRATE !!!!! flyway: ${config.strategyName} ${Instant.now()} ...")
                try {
                    flyway.migrate()
                    Unit
                } catch (all: Throwable) {
                    logger.error("flyway FAILED! ${all.message}", all)
                    throw  all
                }
            }
            FlywayStrategyName.REPAIR -> {
                logger.warn("flyway: ${config.strategyName} - Hope, you know what your doing ;) ...")
                flyway.repair()
            }
            FlywayStrategyName.BASELINE -> {
                logger.warn("flyway: ${config.strategyName} - Hope, you know what your doing ;) ...")
                flyway.baseline()
            }
        }
    }
}
