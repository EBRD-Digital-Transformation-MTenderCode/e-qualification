package com.procurement.qualification.domain.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class Pmd(@JsonValue override val key: String) : EnumElementProvider.Key {

    GPA("GPA");

    override fun toString(): String = key

    companion object : EnumElementProvider<Pmd>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = orThrow(name)
    }
}
