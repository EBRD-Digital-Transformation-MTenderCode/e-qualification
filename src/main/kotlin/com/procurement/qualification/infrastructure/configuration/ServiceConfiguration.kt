package com.procurement.qualification.infrastructure.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.qualification.application.repository.PeriodRepository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(
    basePackages = [
        "com.procurement.qualification.infrastructure.service",
        "com.procurement.qualification.application.service",
        "com.procurement.qualification.infrastructure.handler"
    ]
)
class ServiceConfiguration {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var periodRepository: PeriodRepository

}
