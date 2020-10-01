package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.application.model.parsePmd
import com.procurement.qualification.domain.enums.ProcurementMethodDetails
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.fail.error.DataErrors

class AnalyzeQualificationsForInvitationParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val pmd: ProcurementMethodDetails,
    val country: String
) {
    companion object {
        val allowedPmd = ProcurementMethodDetails.allowedElements.filter {
            when (it) {
                ProcurementMethodDetails.RT, ProcurementMethodDetails.TEST_RT,
                ProcurementMethodDetails.GPA, ProcurementMethodDetails.TEST_GPA -> true

                ProcurementMethodDetails.CD, ProcurementMethodDetails.TEST_CD,
                ProcurementMethodDetails.CF, ProcurementMethodDetails.TEST_CF,
                ProcurementMethodDetails.DA, ProcurementMethodDetails.TEST_DA,
                ProcurementMethodDetails.DC, ProcurementMethodDetails.TEST_DC,
                ProcurementMethodDetails.FA, ProcurementMethodDetails.TEST_FA,
                ProcurementMethodDetails.IP, ProcurementMethodDetails.TEST_IP,
                ProcurementMethodDetails.MV, ProcurementMethodDetails.TEST_MV,
                ProcurementMethodDetails.NP, ProcurementMethodDetails.TEST_NP,
                ProcurementMethodDetails.OF, ProcurementMethodDetails.TEST_OF,
                ProcurementMethodDetails.OP, ProcurementMethodDetails.TEST_OP,
                ProcurementMethodDetails.OT, ProcurementMethodDetails.TEST_OT,
                ProcurementMethodDetails.SV, ProcurementMethodDetails.TEST_SV -> false
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