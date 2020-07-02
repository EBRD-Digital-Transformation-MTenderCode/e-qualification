package com.procurement.qualification.domain.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class OperationType(@JsonValue override val key: String) : EnumElementProvider.Key {

    QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST("qualificationDeclareNonConflictOfInterest"),
    QUALIFICATION("qualification"),
    QUALIFICATION_CONSIDERATION("qualificationConsideration");

    override fun toString(): String = key

    companion object : EnumElementProvider<OperationType>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = orThrow(name)
    }
}
