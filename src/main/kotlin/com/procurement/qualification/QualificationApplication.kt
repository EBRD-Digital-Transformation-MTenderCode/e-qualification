package com.procurement.qualification

import com.procurement.qualification.infrastructure.configuration.ApplicationConfiguration
import com.procurement.qualification.infrastructure.configuration.properties.GlobalProperties2
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackageClasses = [ApplicationConfiguration::class])
class QualificationApplication

fun main(args: Array<String>) {
    runApplication<QualificationApplication>(*args)
    println("Ran service ${GlobalProperties2.service.name}:${GlobalProperties2.service.version}")
}
