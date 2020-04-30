package com.procurement.qualification.infrastructure.configuration

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = ["com.procurement.qualification.infrastructure.web.controller"])
class WebConfiguration
