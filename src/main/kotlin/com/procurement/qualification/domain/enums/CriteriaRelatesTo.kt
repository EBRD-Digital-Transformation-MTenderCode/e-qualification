package com.procurement.qualification.domain.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class CriteriaRelatesTo(@JsonValue override val key: String) : EnumElementProvider.Key {

    AWARD("award"),
    ITEM("item"),
    LOT("lot"),
    TENDER("tender"),
    TENDERER("tenderer"),
    QUALIFICATION("qualification");

    override fun toString(): String = key

    companion object : EnumElementProvider<CriteriaRelatesTo>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = orThrow(name)
    }
}
