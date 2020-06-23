package com.procurement.qualification.domain.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class RequirementDataType(@JsonValue override val key: String) : EnumElementProvider.Key {

    BOOLEAN("boolean"),
    INTEGER("integer"),
    NUMBER("number"),
    STRING("string");

    override fun toString(): String = key

    companion object : EnumElementProvider<RequirementDataType>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = orThrow(name)
    }
}
