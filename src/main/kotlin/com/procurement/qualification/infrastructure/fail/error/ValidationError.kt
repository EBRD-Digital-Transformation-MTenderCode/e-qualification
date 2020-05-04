package com.procurement.qualification.infrastructure.fail.error

import com.procurement.qualification.domain.enums.ProcurementMethod
import com.procurement.qualification.infrastructure.fail.Fail

sealed class ValidationError(
    numberError: String,
    override val description: String,
    val entityId: String? = null
) : Fail.Error("VR") {
    override val code: String = prefix + numberError

    sealed class CommandError(
        numberError: String, description: String
    ) : ValidationError(".COM-$numberError", description) {

        class InvalidPeriod() : CommandError(
            numberError = "7.1.3",
            description = "Period start date must precede end date."
        )

        class PeriodRuleNotFound(country: String, pmd: ProcurementMethod) : CommandError(
            numberError = "7.1.4",
            description = "No period duration rule found for country '$country' and pmd '$pmd'."
        )

        class InvalidPeriodTerm() : CommandError(
            numberError = "7.1.5",
            description = "Period duration is invalid."
        )
    }
}