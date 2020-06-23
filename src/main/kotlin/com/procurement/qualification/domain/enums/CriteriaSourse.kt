package com.procurement.qualification.domain.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class CriteriaSource(@JsonValue override val key: String) : EnumElementProvider.Key {

    PROCURING_ENTITY("procuringEntity"),
    TENDERER("tenderer");

    override fun toString(): String = key

    companion object : EnumElementProvider<CriteriaSource>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = orThrow(name)
    }
}

