package com.procurement.qualification.domain.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class QualificationStatusDetails(@JsonValue override val key: String) : EnumElementProvider.Key {

    ACTIVE("active"),
    AWAITING("awaiting"),
    CONSIDERATION("consideration"),
    UNSUCCESSFUL("unsuccessful");

    override fun toString(): String = key

    companion object : EnumElementProvider<QualificationStatusDetails>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = orThrow(name)
    }
}
