package com.procurement.qualification.infrastructure.fail.error

import com.procurement.qualification.infrastructure.fail.Fail

class BadRequest(override val description: String = "Invalid json") : Fail.Error("RQ-") {
    private val numberError = "1"
    override val code: String = "${prefix}${numberError}"
}
