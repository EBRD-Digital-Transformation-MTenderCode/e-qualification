package com.procurement.qualification.infrastructure.web.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.qualification.domain.enums.EnumElementProvider
import com.procurement.qualification.infrastructure.web.dto.Action

enum class Command2Type(@JsonValue override val key: String) : Action, EnumElementProvider.Key {

    FIND_QUALIFICATION_IDS("findQualificationIds"),
    CREATE_QUALIFICATIONS("createQualifications"),
    RANK_QUALIFICATIONS("rankQualifications"),
    START_QUALIFICATION_PERIOD("startQualificationPeriod"),
    CHECK_ACCESS_TO_QUALIFICATION("checkAccessToQualification"),
    CHECK_QUALIFICATION_STATE("checkQualificationState"),
    DO_DECLARATION("doDeclaration"),
    CHECK_DECLARATION("checkDeclaration"),
    FIND_REQUIREMENT_RESPONSE_BY_IDS("findRequirementResponseByIds");

    override fun toString(): String = key

    companion object : EnumElementProvider<Command2Type>(info = info()) {
        @JvmStatic
        @JsonCreator
        fun creator(name: String) = Command2Type.orThrow(name)
    }
}




