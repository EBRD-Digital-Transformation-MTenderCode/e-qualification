package com.procurement.qualification.domain.model.qualification

import java.io.Serializable
import java.math.BigDecimal

sealed class RequirementResponseValue : Serializable {
    data class AsBoolean(val value: Boolean) : RequirementResponseValue(), Serializable
    data class AsString(val value: String) : RequirementResponseValue(), Serializable
    data class AsNumber(val value: BigDecimal) : RequirementResponseValue(), Serializable
    data class AsInteger(val value: Long) : RequirementResponseValue(), Serializable
}
