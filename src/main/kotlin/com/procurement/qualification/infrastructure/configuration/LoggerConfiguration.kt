package com.procurement.qualification.infrastructure.configuration

import com.procurement.qualification.application.service.Logger
import com.procurement.qualification.infrastructure.service.CustomLogger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LoggerConfiguration {

    @Bean
    fun logger(): Logger = CustomLogger()
}