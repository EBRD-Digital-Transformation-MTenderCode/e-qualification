package com.procurement.qualification.domain.model.requirementresponse

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.infrastructure.extension.UUID_PATTERN
import com.procurement.qualification.infrastructure.extension.isUUID
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import java.io.Serializable

class RequirementResponseId(private val value: String) : Serializable {

    companion object {
        val pattern: String
            get() = UUID_PATTERN

        fun validation(text: String): Boolean = text.isUUID()

        @JvmStatic
        @JsonCreator
        fun tryCreateOrNull(text: String): RequirementResponseId? = if (validation(text)) RequirementResponseId(text) else null

        fun tryCreate(text: String): Result<RequirementResponseId, DataErrors.Validation.DataFormatMismatch> =
            if (validation(text)) {
                RequirementResponseId(text)
                    .asSuccess()
            } else {
                DataErrors.Validation.DataFormatMismatch(
                    name = "id",
                    actualValue = text,
                    expectedFormat = pattern
                )
                    .asFailure()
            }
    }

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is RequirementResponseId
                && this.value == other.value
        else
            true
    }

    override fun hashCode(): Int = value.hashCode()

    @JsonValue
    override fun toString(): String = value
}