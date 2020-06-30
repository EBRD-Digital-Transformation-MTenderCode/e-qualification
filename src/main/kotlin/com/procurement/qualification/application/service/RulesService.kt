package com.procurement.qualification.application.service

import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.Pmd
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.QualificationRulesEntity
import com.procurement.qualification.infrastructure.repository.CassandraQualificationRulesRepository
import org.springframework.stereotype.Service

interface RulesService {

    fun findValidStates(
        country: String,
        pmd: Pmd,
        operationType: OperationType
    ): Result<QualificationRulesEntity, Fail>
}

@Service
class RulesServiceImpl(private val qualificationRulesRepository: CassandraQualificationRulesRepository) : RulesService {

    companion object {
        private const val VALID_STATES_PARAMETER = "validStates"
    }

    override fun findValidStates(
        country: String,
        pmd: Pmd,
        operationType: OperationType
    ): Result<QualificationRulesEntity, Fail> {
        return qualificationRulesRepository.findBy(
            country = country,
            operationType = operationType,
            pmd = pmd,
            parameter = VALID_STATES_PARAMETER
        )
    }
}
