package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.application.model.parseQualificationId
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.infrastructure.fail.error.DataErrors

class DoConsiderationParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val qualifications: List<Qualification>
) {
    companion object {
        fun tryCreate(
            cpid: String, ocid: String, qualifications: List<Qualification>
        ): Result<DoConsiderationParams, DataErrors> {
            val parsedCpid = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val parsedOcid = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            if (qualifications.isEmpty())
                return DataErrors.Validation.EmptyArray(name = "qualifications").asFailure()

            return DoConsiderationParams(
                cpid = parsedCpid,
                ocid = parsedOcid,
                qualifications = qualifications
            ).asSuccess()
        }
    }

    class Qualification private constructor(
        val id: QualificationId
    ) {
        companion object {
            fun tryCreate(id: String): Result<Qualification, DataErrors> {
                val parsedId = parseQualificationId(id)
                    .orForwardFail { fail -> return fail }
                return Qualification(id = parsedId).asSuccess()
            }
        }
    }
}