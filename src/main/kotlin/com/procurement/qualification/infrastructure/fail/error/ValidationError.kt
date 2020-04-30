package com.procurement.qualification.infrastructure.fail.error

import com.procurement.qualification.infrastructure.fail.Fail

sealed class ValidationError(
    numberError: String,
    override val description: String,
    val entityId: String? = null
) : Fail.Error("VR-") {
    override val code: String = prefix + numberError
}