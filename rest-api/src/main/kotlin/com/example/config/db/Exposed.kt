package com.example.config.db

import com.zaxxer.hikari.HikariDataSource
import mu.KLogging
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
import org.springframework.transaction.annotation.EnableTransactionManagement

private typealias PlatformDataSource = HikariDataSource

@Configuration
@EnableTransactionManagement
class Exposed {

    @Bean
    fun transactionManager(dataSource: PlatformDataSource): SpringTransactionManager =
            SpringTransactionManager(dataSource)
                    .also {
                        logger.info {
                            "=== USE SQL datasource ${dataSource.toDetailsText()}"
                        }
                    }

    @Bean // PersistenceExceptionTranslationPostProcessor with proxyTargetClass=false, see https://github.com/spring-projects/spring-boot/issues/1844
    fun persistenceExceptionTranslationPostProcessor() = PersistenceExceptionTranslationPostProcessor()

    companion object : KLogging()
}

private fun PlatformDataSource.toDetailsText(): String =
        "user: $username url: $jdbcUrl pool: $poolName maxPoolSize: $maximumPoolSize minIdle: $minimumIdle"
