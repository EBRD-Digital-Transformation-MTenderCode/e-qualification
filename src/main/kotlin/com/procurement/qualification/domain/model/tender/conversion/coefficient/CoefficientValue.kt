package com.procurement.qualification.domain.model.tender.conversion.coefficient

import java.io.Serializable
import java.math.BigDecimal

sealed class CoefficientValue : Serializable {
    data class AsBoolean(val value: Boolean) : CoefficientValue(), Serializable
    data class AsString(val value: String) : CoefficientValue(), Serializable
    data class AsNumber(val value: BigDecimal) : CoefficientValue(), Serializable
    data class AsInteger(val value: Long) : CoefficientValue(), Serializable
}
