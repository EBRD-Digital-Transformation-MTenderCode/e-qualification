package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.application.model.parseQualificationId
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.Owner
import com.procurement.qualification.domain.model.Token
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.tryOwner
import com.procurement.qualification.domain.model.tryToken
import com.procurement.qualification.infrastructure.fail.error.DataErrors

class CheckAccessToQualificationParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val token: Token,
    val owner: Owner,
    val qualificationId: QualificationId
) {
    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            token: String,
            owner: String,
            qualificationId: String
        ): Result<CheckAccessToQualificationParams, DataErrors> {

            val parsedCpid = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val parsedOcid = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            val parsedToken = token.tryToken()
                .orForwardFail { fail -> return fail }

            val parsedOwner = owner.tryOwner()
                .orForwardFail { fail -> return fail }

            val parsedId = parseQualificationId(value = qualificationId)
                .orForwardFail { fail -> return fail }

            return CheckAccessToQualificationParams(
                cpid = parsedCpid,
                ocid = parsedOcid,
                token = parsedToken,
                owner = parsedOwner,
                qualificationId = parsedId
            )
                .asSuccess()
        }
    }
}
