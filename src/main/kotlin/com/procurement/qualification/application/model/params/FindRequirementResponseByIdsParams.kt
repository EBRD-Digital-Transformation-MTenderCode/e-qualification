package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.application.model.parseQualificationId
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.infrastructure.fail.error.DataErrors

class FindRequirementResponseByIdsParams private constructor(
    val requirementResponseIds: List<String>,
    val qualificationId: QualificationId,
    val cpid: Cpid,
    val ocid: Ocid
) {
    companion object {
        fun tryCreate(
            requirementResponseIds: List<String>,
            qualificationId: String,
            cpid: String,
            ocid: String
        ): Result<FindRequirementResponseByIdsParams, DataErrors> {

            val parsedCpid = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val parsedOcid = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            val parsedId = parseQualificationId(value = qualificationId)
                .orForwardFail { fail -> return fail }

            return FindRequirementResponseByIdsParams(
                cpid = parsedCpid,
                ocid = parsedOcid,
                qualificationId = parsedId,
                requirementResponseIds = requirementResponseIds
            )
                .asSuccess()
        }
    }
}
