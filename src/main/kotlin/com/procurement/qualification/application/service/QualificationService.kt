package com.procurement.qualification.application.service

import com.procurement.qualification.application.model.params.CheckAccessToQualificationParams
import com.procurement.qualification.application.model.params.FindQualificationIdsParams
import com.procurement.qualification.application.repository.QualificationRepository
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.functional.asValidationFailure
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.fail.error.ValidationError
import org.springframework.stereotype.Service

interface QualificationService {

    fun findQualificationIds(params: FindQualificationIdsParams): Result<List<QualificationId>, Fail>
    fun checkAccessToQualification(params: CheckAccessToQualificationParams): ValidationResult<Fail>
}

@Service
class QualificationServiceImpl(
    val qualificationRepository: QualificationRepository,
    val transform: Transform
) : QualificationService {

    override fun findQualificationIds(params: FindQualificationIdsParams): Result<List<QualificationId>, Fail> {

        val qualificationEntities = qualificationRepository.findBy(cpid = params.cpid, ocid = params.ocid)
            .orForwardFail { fail -> return fail }

        val qualifications = qualificationEntities
            .map {
                transform.tryDeserialization(value = it.jsonData, target = Qualification::class.java)
                    .doOnError { fail ->
                        return Fail.Incident.Database.DatabaseParsing(exception = fail.exception)
                            .asFailure()
                    }
                    .get
            }

        if (params.states.isEmpty())
            return qualifications.map { it.id }
                .asSuccess()

        val filteredQualifications = qualifications.filter { qualification ->
            compareStatuses(qualification = qualification, states = params.states)
        }

        return filteredQualifications.map { it.id }
            .asSuccess()
    }

    override fun checkAccessToQualification(params: CheckAccessToQualificationParams): ValidationResult<Fail> {

        val cpid = params.cpid
        val ocid = params.ocid
        val qualificationId = params.qualificationId

        val qualificationEntity = qualificationRepository.findBy(
            cpid = cpid,
            ocid = ocid,
            qualificationId = qualificationId
        )
            .doReturn { fail -> return ValidationResult.error(fail) }
            ?: return ValidationError.QualificationNotFoundByCheckAccessToQualification(
                cpid = cpid,
                ocid = ocid,
                qualificationId = qualificationId
            )
                .asValidationFailure()

        val qualification = qualificationEntity
            .let {
                transform.tryDeserialization(value = it.jsonData, target = Qualification::class.java)
                    .doReturn { fail ->
                        return Fail.Incident.Database.DatabaseParsing(exception = fail.exception)
                            .asValidationFailure()
                    }
            }


        if (params.token != qualification.token)
            return ValidationError.InvalidTokenOnCheckAccessToQualification(cpid = params.cpid, token = params.token)
                .asValidationFailure()

        if(params.owner != qualification.owner)
            return ValidationError.InvalidOwnerOnCheckAccessToQualification(cpid = params.cpid, owner = params.owner)
                .asValidationFailure()

        return ValidationResult.ok()
    }

    private fun compareStatuses(qualification: Qualification, states: List<FindQualificationIdsParams.State>): Boolean {
        return states.any { state ->
            state.status == qualification.status ||
                state.statusDetails == qualification.statusDetails
        }
    }
}
