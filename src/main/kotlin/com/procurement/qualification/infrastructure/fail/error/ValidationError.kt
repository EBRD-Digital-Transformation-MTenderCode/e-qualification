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

        class InvalidPeriodOnCheckPeriod() : CommandError(
            numberError = "7.3.2",
            description = "Period start date must precede end date."
        )

        class InvalidPeriodEndDate() : CommandError(
            numberError = "7.3.3",
            description = "Period end date must be equal or greater than previously stored period end date."
        )

        class InvalidPeriodStartDateOnCheckPeriod2() : CommandError(
            numberError = "7.4.3",
            description = "Period date must be after stored period start date."
        )

        class InvalidPeriodEndDateOnCheckPeriod2() : CommandError(
            numberError = "7.4.3",
            description = "Period end date must precede stored period end date."
        )
    }

}