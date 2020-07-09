package com.procurement.qualification.domain.model.submission

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.qualification.infrastructure.extension.UUID_PATTERN
import com.procurement.qualification.infrastructure.extension.isUUID
import java.util.*

class SubmissionId private constructor(private val value: String) {

    companion object {
        val pattern: String
            get() = UUID_PATTERN

        fun validation(text: String): Boolean = text.isUUID()

        @JvmStatic
        @JsonCreator
        fun tryCreateOrNull(text: String): SubmissionId? = if (validation(text)) SubmissionId(text) else null

        fun generate(): SubmissionId = SubmissionId(UUID.randomUUID().toString())
    }

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is SubmissionId
                && this.value == other.value
        else
            true
    }

    override fun hashCode(): Int = value.hashCode()

    @JsonValue
    override fun toString(): String = value
}
