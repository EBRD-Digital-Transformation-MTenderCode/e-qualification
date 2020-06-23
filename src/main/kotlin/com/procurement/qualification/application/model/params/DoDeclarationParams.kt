package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.application.model.parseQualificationId
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.infrastructure.fail.error.DataErrors

class DoDeclarationParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val qualifications: List<Qualification>
) {

    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            qualifications: List<Qualification>
        ): Result<DoDeclarationParams, DataErrors> {

            val parsedCpid = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val parsedOcid = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            return DoDeclarationParams(cpid = parsedCpid, ocid = parsedOcid, qualifications = qualifications)
                .asSuccess()
        }
    }

    class Qualification private constructor(
        val id: QualificationId,
        val requirementResponses: List<RequirementResponse>
    ) {

        companion object {
            fun tryCreate(
                id: String,
                requirementResponses: List<RequirementResponse>
            ): Result<Qualification, DataErrors> {
                val parsedId = parseQualificationId(value = id)
                    .orForwardFail { fail -> return fail }
                return Qualification(id = parsedId, requirementResponses = requirementResponses)
                    .asSuccess()
            }
        }

        class RequirementResponse private constructor(
            val id: String,
            val value: RequirementResponseValue,
            val relatedTenderer: RelatedTenderer,
            val requirement: Requirement,
            val responder: Responder
        ) {
            companion object {
                fun tryCreate(
                    id: String,
                    value: RequirementResponseValue,
                    relatedTenderer: RelatedTenderer,
                    requirement: Requirement,
                    responder: Responder
                ): Result<RequirementResponse, DataErrors> {
                    return RequirementResponse(
                        id = id,
                        value = value,
                        relatedTenderer = relatedTenderer,
                        requirement = requirement,
                        responder = responder
                    )
                        .asSuccess()
                }
            }

            class RelatedTenderer private constructor(val id: String) {
                companion object {
                    fun tryCreate(id: String): Result<RelatedTenderer, DataErrors> {
                        return RelatedTenderer(id = id)
                            .asSuccess()
                    }
                }
            }

            class Requirement private constructor(val id: String) {
                companion object {
                    fun tryCreate(id: String): Result<Requirement, DataErrors> {
                        return Requirement(id = id)
                            .asSuccess()
                    }
                }
            }

            class Responder private constructor(val id: String, val name: String) {
                companion object {
                    fun tryCreate(id: String, name: String): Result<Responder, DataErrors> {
                        return Responder(id = id, name = name)
                            .asSuccess()
                    }
                }
            }
        }
    }
}
