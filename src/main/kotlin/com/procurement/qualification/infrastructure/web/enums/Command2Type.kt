package com.procurement.qualification.infrastructure.web.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.qualification.domain.enums.EnumElementProvider
import com.procurement.qualification.infrastructure.web.dto.Action

enum class Command2Type(@JsonValue override val key: String) : Action, EnumElementProvider.Key {

    CHECK_ACCESS_TO_QUALIFICATION("checkAccessToQualification"),
    CHECK_DECLARATION("checkDeclaration"),
    CHECK_QUALIFICATION_STATE("checkQualificationState"),
    CREATE_QUALIFICATIONS("createQualifications"),
    DO_CONSIDERATION("doConsideration"),
    DO_DECLARATION("doDeclaration"),
    DO_QUALIFICATION("doQualification"),
    FIND_QUALIFICATION_IDS("findQualificationIds"),
    FIND_REQUIREMENT_RESPONSE_BY_IDS("findRequirementResponseByIds"),
    RANK_QUALIFICATIONS("rankQualifications"),
    SET_NEXT_FOR_QUALIFICATION("setNextForQualification"),
    START_QUALIFICATION_PERIOD("startQualificationPeriod"),
    CHECK_QUALIFICATION_PERIOD("checkQualificationPeriod"),
    CHECK_QUALIFICATIONS_FOR_PROTOCOL("checkQualificationsForProtocol"),
    ANALYZE_QUALIFICATION_FOR_INVITATIONS("analyzeQualificationsForInvitation");

    override fun toString(): String = key

    companion object : EnumElementProvider<Command2Type>(info = info()) {
        @JvmStatic
        @JsonCreator
        fun creator(name: String) = Command2Type.orThrow(name)
    }
}




