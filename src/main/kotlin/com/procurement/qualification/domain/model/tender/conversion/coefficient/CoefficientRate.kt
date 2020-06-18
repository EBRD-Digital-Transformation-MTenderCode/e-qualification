package com.procurement.qualification.domain.model.tender.conversion.coefficient

import java.io.Serializable
import java.math.BigDecimal

data class CoefficientRate(val rate: BigDecimal) : Serializable {

    operator fun times(other: CoefficientRate) = CoefficientRate(rate.times(other.rate))
}
