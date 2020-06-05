package com.procurement.qualification.domain.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ConversionRelatesTo(@JsonValue override val key: String) : EnumElementProvider.Key {

    REQUIREMENT("requirement");

    override fun toString(): String = key

    companion object : EnumElementProvider<ConversionRelatesTo>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = orThrow(name)
    }
}
