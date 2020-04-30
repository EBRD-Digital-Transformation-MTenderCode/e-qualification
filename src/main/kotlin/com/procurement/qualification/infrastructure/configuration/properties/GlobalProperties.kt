package com.procurement.qualification.infrastructure.configuration.properties

import com.procurement.qualification.infrastructure.web.dto.command.ApiVersion

object GlobalProperties {
    const val serviceId = "" //TODO()

    object App {
        val apiVersion = ApiVersion.V_0_0_1
    }
}
