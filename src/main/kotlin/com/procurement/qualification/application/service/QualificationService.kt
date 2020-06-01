package com.procurement.qualification.application.service

import com.procurement.qualification.application.model.params.FindQualificationIdsParams
import com.procurement.qualification.application.repository.QualificationRepository
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.infrastructure.fail.Fail
import org.springframework.stereotype.Service

interface QualificationService {

    fun findQualificationIds(params: FindQualificationIdsParams): Result<List<QualificationId>, Fail>
}

@Service
class QualificationServiceImpl(
    val qualificationRepository: QualificationRepository,
    val transform: Transform
) : QualificationService {

    override fun findQualificationIds(params: FindQualificationIdsParams): Result<List<QualificationId>, Fail> {

        val qualificationEntities = qualificationRepository.findBy(cpid = params.cpid, ocid = params.ocid)
            .orForwardFail { fail -> return fail }

        if (qualificationEntities.isEmpty())
            return emptyList<QualificationId>()
                .asSuccess()

        val qualifications = qualificationEntities
            .map {
                transform.tryMapping(it.jsonData, Qualification::class.java)
                    .doOnError { fail ->
                        return Fail.Incident.Database.DatabaseParsing(exception = fail.exception)
                            .asFailure()
                    }
                    .get
            }

        
    }
}
