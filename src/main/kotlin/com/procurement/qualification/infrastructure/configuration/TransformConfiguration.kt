package com.procurement.qualification.infrastructure.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.infrastructure.bind.jackson.configuration
import com.procurement.qualification.infrastructure.service.JacksonJsonTransform
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TransformConfiguration {

    @Bean
    fun transform(): Transform = JacksonJsonTransform(mapper = ObjectMapper().apply { configuration() })
}
