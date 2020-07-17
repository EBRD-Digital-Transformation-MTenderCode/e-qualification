package com.procurement.qualification.application.service

import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.Pmd
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.state.States
import com.procurement.qualification.domain.util.extension.tryToLong
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.fail.error.ValidationError
import com.procurement.qualification.infrastructure.repository.CassandraQualificationRulesRepository
import org.springframework.stereotype.Service

interface RulesService {

    fun findValidStates(
        country: String,
        pmd: Pmd,
        operationType: OperationType
    ): Result<States, Fail>

    fun findMinimumQualificationQuantity(
        country: String,
        pmd: Pmd
    ): Result<Long?, Fail>
}

@Service
class RulesServiceImpl(
    private val qualificationRulesRepository: CassandraQualificationRulesRepository,
    val transform: Transform
) : RulesService {

    companion object {
        private const val VALID_STATES_PARAMETER = "validStates"
        private const val QUALIFICATION_MINIMUM_PARAMETER = "minQtyQualificationsForInvitation"
    }

    override fun findValidStates(
        country: String,
        pmd: Pmd,
        operationType: OperationType
    ): Result<States, Fail> {

        val states = qualificationRulesRepository.findBy(
            country = country,
            operationType = operationType,
            pmd = pmd,
            parameter = VALID_STATES_PARAMETER
        )
            .orForwardFail { fail -> return fail }
            ?: return ValidationError.QualificationStatesNotFound(country, pmd, operationType)
                .asFailure()


        return states
            .convert()
            .orForwardFail { fail -> return fail }
            .asSuccess()
    }

    private fun String.convert(): Result<States, Fail.Incident.Database.DatabaseParsing> =
        this.let {
            transform.tryDeserialization(value = it, target = States::class.java)
                .doReturn { fail ->
                    return Fail.Incident.Database.DatabaseParsing(exception = fail.exception)
                        .asFailure()
                }
        }
            .asSuccess()

    override fun findMinimumQualificationQuantity(
        country: String,
        pmd: Pmd
    ): Result<Long?, Fail> {

        val minimumQuantity = qualificationRulesRepository.findBy(
            country = country,
            pmd = pmd,
            parameter = QUALIFICATION_MINIMUM_PARAMETER
        )
            .orForwardFail { fail -> return fail }

        return minimumQuantity
            ?.tryToLong()
            ?.orForwardFail { fail -> return fail }
            .asSuccess()
    }
}
