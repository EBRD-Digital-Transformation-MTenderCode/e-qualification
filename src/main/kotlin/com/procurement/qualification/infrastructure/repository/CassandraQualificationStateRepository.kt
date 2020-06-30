package com.procurement.qualification.infrastructure.repository

import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.procurement.qualification.application.repository.QualificationStateRepository
import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.Pmd
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.infrastructure.extension.cassandra.tryExecute
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.QualificationStateEntity
import org.springframework.stereotype.Repository

@Repository
class CassandraQualificationStateRepository(private val session: Session) : QualificationStateRepository {

    companion object {
        private const val KEYSPACE = "qualification"
        private const val TABLE_NAME = "qualification_states"
        private const val COLUMN_COUNTRY = "country"
        private const val COLUMN_PMD = "pmd"
        private const val COLUMN_OPERATION_TYPE = "operation_type"
        private const val COLUMN_PARAMETER = "parameter"
        private const val COLUMN_VALUE = "value"

        private const val VALID_STATES_PARAMETER = "validStates"

        private const val FIND_BY_COUNTRY_PMD_OPERATION_TYPE_CQL = """
            SELECT $COLUMN_COUNTRY,
                   $COLUMN_PMD,
                   $COLUMN_OPERATION_TYPE,
                   $COLUMN_PARAMETER,
                   $COLUMN_VALUE
              FROM $KEYSPACE.$TABLE_NAME
             WHERE $COLUMN_COUNTRY=?
               AND $COLUMN_PMD=?
               AND $COLUMN_OPERATION_TYPE=?
               AND $COLUMN_PARAMETER=?
        """
    }

    private val preparedFindByCountryPmdOperationType = session.prepare(FIND_BY_COUNTRY_PMD_OPERATION_TYPE_CQL)

    override fun findValidStatesBy(
        country: String,
        pmd: Pmd,
        operationType: OperationType
    ): Result<QualificationStateEntity, Fail> {

        val updatedOperationType = when (operationType) {
            OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION -> operationType
        }
        return findBy(country = country, pmd = pmd, operationType = updatedOperationType, parameter = VALID_STATES_PARAMETER)
            .orForwardFail { error -> return error }
            .asSuccess()
    }

    private fun findBy(
        country: String,
        pmd: Pmd,
        operationType: OperationType,
        parameter: String
    ): Result<QualificationStateEntity, Fail> {
        val query = preparedFindByCountryPmdOperationType.bind()
            .apply {
                setString(COLUMN_COUNTRY, country)
                setString(COLUMN_PMD, pmd.toString())
                setString(COLUMN_OPERATION_TYPE, operationType.toString())
                setString(COLUMN_PARAMETER, parameter)
            }

        return query.tryExecute(session)
            .orForwardFail { error -> return error }
            .one()
            .converter()
            .orForwardFail { error -> return error }
            .asSuccess()
    }

    private fun Row.converter(): Result<QualificationStateEntity, Fail> {
        val pmd = this.getString(COLUMN_PMD)
        val pmdParsed = Pmd.orNull(pmd)
            ?: return Result.failure(Fail.Incident.Database.Parsing(column = COLUMN_PMD, value = pmd))

        val operationType = this.getString(COLUMN_OPERATION_TYPE)
        val operationTypeParsed = OperationType.orNull(operationType)
            ?: return Result.failure(
                Fail.Incident.Database.Parsing(
                    column = COLUMN_OPERATION_TYPE,
                    value = operationType
                )
            )
        return QualificationStateEntity(
            country = this.getString(COLUMN_COUNTRY),
            pmd = pmdParsed,
            operationType = operationTypeParsed,
            value = this.getString(COLUMN_VALUE),
            parameter = this.getString(COLUMN_PARAMETER)
        )
            .asSuccess()
    }
}