package com.procurement.qualification.infrastructure.configuration

import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.infrastructure.repository.CassandraTestContainer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.wait.strategy.Wait

@TestConfiguration
class DatabaseTestConfiguration {
    @Bean
    fun container() = CassandraTestContainer("3.11")
        .apply {
            setWaitStrategy(Wait.forListeningPort())
            start()
        }

    @Bean
    fun transform() : Transform = TransformConfiguration().transform()
}
