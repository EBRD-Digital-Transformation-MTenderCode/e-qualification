package com.procurement.qualification.infrastructure.bind.requirement.value

import com.fasterxml.jackson.databind.module.SimpleModule
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue

class RequirementValueModule : SimpleModule() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 1L
    }

    init {
        addSerializer(RequirementResponseValue::class.java, RequirementValueSerializer())
        addDeserializer(RequirementResponseValue::class.java, RequirementValueDeserializer())
    }
}
