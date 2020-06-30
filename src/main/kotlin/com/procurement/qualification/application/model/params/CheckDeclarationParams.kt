package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseEnum
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.application.model.parseQualificationId
import com.procurement.qualification.domain.enums.RequirementDataType
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.requirement.RequirementId
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.requirementresponse.RequirementResponseId
import com.procurement.qualification.domain.model.requirementresponse.tryCreateRequirementResponseId
import com.procurement.qualification.infrastructure.fail.error.DataErrors

class CheckDeclarationParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val qualificationId: QualificationId,
    val requirementResponse: RequirementResponse,
    val criteria: List<Criteria>
) {

    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            qualificationId: String,
            requirementResponse: RequirementResponse,
            criteria: List<Criteria>
        ): Result<CheckDeclarationParams, DataErrors> {

            val parsedCpid = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val parsedOcid = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            val parsedId = parseQualificationId(value = qualificationId)
                .orForwardFail { fail -> return fail }

            return CheckDeclarationParams(
                cpid = parsedCpid,
                ocid = parsedOcid,
                qualificationId = parsedId,
                criteria = criteria,
                requirementResponse = requirementResponse
            )
                .asSuccess()
        }
    }

    class RequirementResponse private constructor(
        val id: RequirementResponseId,
        val value: RequirementResponseValue,
        val relatedTendererId: String,
        val responderId: String,
        val requirementId: String
    ) {
        companion object {
            fun tryCreate(
                id: String,
                value: RequirementResponseValue,
                relatedTendererId: String,
                responderId: String,
                requirementId: String
            ): Result<RequirementResponse, DataErrors> {

                val parsedId = tryCreateRequirementResponseId(value = id)
                    .orForwardFail { fail -> return fail }

                return RequirementResponse(parsedId, value, relatedTendererId, responderId, requirementId)
                    .asSuccess()
            }
        }
    }

    class Criteria private constructor(
        val id: String,
        val requirementGroups: List<RequirementGroup>
    ) {

        companion object {
            fun tryCreate(
                id: String,
                requirementGroups: List<RequirementGroup>
            ): Result<Criteria, DataErrors> {
                return Criteria(id, requirementGroups)
                    .asSuccess()
            }
        }

        class RequirementGroup private constructor(
            val id: String,
            val requirements: List<Requirement>
        ) {

            companion object {
                fun tryCreate(
                    id: String,
                    requirements: List<Requirement>
                ): Result<RequirementGroup, DataErrors> {
                    return RequirementGroup(id, requirements)
                        .asSuccess()
                }
            }

            class Requirement private constructor(
                val id: RequirementId,
                val dataType: RequirementDataType
            ) {
                companion object {

                    private val allowedDataType = RequirementDataType.allowedElements
                        .filter {
                            when (it) {
                                RequirementDataType.BOOLEAN,
                                RequirementDataType.INTEGER,
                                RequirementDataType.NUMBER,
                                RequirementDataType.STRING -> true
                            }
                        }
                        .toSet()

                    fun tryCreate(
                        id: String,
                        dataType: String
                    ): Result<Requirement, DataErrors> {

                        val parsedDataType = parseEnum(
                            value = dataType,
                            attributeName = "dataType",
                            target = RequirementDataType,
                            allowedEnums = allowedDataType
                        )
                            .orForwardFail { fail -> return fail }

                        return Requirement(id, parsedDataType)
                            .asSuccess()
                    }
                }
            }
        }
    }
}
