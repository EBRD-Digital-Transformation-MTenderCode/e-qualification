package com.procurement.qualification.domain.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class DocumentType(@JsonValue override val key: String) : EnumElementProvider.Key {

    CONFLICT_OF_INTEREST("conflictOfInterest"),
    EVALUATION_REPORTS("evaluationReports"),
    NOTICE("notice");

    override fun toString(): String = key

    companion object : EnumElementProvider<DocumentType>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = orThrow(name)
    }
}
