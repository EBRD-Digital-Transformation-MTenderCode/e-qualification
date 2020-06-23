package com.procurement.qualification.infrastructure.bind.coefficient.rate

import com.fasterxml.jackson.databind.module.SimpleModule
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientRate

class CoefficientRateModule : SimpleModule() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 1L
    }

    init {
        addSerializer(CoefficientRate::class.java, CoefficientRateSerializer())
        addDeserializer(CoefficientRate::class.java, CoefficientRateDeserializer())
    }
}
