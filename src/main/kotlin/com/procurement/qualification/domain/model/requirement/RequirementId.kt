package com.procurement.qualification.domain.model.requirement

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.io.Serializable

class RequirementId private constructor(private val value: String) : Serializable {

    @JsonValue
    override fun toString(): String = value

    override fun equals(other: Any?): Boolean = if (this !== other)
        other is RequirementId
            && this.value == other.value
    else
        true

    override fun hashCode(): Int = value.hashCode()

    companion object {

        @JvmStatic
        @JsonCreator
        fun parse(text: String): RequirementId? = if (text.isBlank())
            null
        else
            RequirementId(text)
    }
}

