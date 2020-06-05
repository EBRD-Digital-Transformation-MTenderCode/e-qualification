package com.procurement.qualification.domain.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class QualificationSystemMethod(@JsonValue override val key: String) : EnumElementProvider.Key {

    AUTOMATED("automated"),
    MANUAL("manual");

    override fun toString(): String = key

    companion object : EnumElementProvider<QualificationSystemMethod>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = orThrow(name)
    }
}
