package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.application.model.parsePmd
import com.procurement.qualification.domain.enums.Pmd
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.fail.error.DataErrors

class AnalyzeQualificationsForInvitationParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val pmd: Pmd,
    val country: String
) {
    companion object {
        val allowedPmd = Pmd.allowedElements.filter {
            when (it) {
                Pmd.GPA, Pmd.TEST_GPA -> true
            }
        }

        fun tryCreate(
            cpid: String, ocid: String, pmd: String, country: String
        ): Result<AnalyzeQualificationsForInvitationParams, DataErrors> {

            val cpidParsed = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val ocidParsed = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            val pmdParsed = parsePmd(value = pmd, allowedEnums = allowedPmd)
                .orForwardFail { fail -> return fail }

            return AnalyzeQualificationsForInvitationParams(
                cpid = cpidParsed,
                ocid = ocidParsed,
                pmd = pmdParsed,
                country = country
            ).asSuccess()
        }
    }
}