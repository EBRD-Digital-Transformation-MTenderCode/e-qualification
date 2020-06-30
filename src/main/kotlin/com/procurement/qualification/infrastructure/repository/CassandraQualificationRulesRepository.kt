package com.procurement.qualification.infrastructure.repository

import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.procurement.qualification.application.repository.QualificationRulesRepository
import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.Pmd
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.infrastructure.extension.cassandra.tryExecute
import com.procurement.qualification.infrastructure.fail.Fail
import org.springframework.stereotype.Repository

@Repository
class CassandraQualificationRulesRepository(private val session: Session) : QualificationRulesRepository {

    companion object {
        private const val KEYSPACE = "qualification"
        private const val TABLE_NAME = "qualification_rules"
        private const val COLUMN_COUNTRY = "country"
        private const val COLUMN_PMD = "pmd"
        private const val COLUMN_OPERATION_TYPE = "operation_type"
        private const val COLUMN_PARAMETER = "parameter"
        private const val COLUMN_VALUE = "value"

        private const val FIND_BY_COUNTRY_PMD_OPERATION_TYPE_CQL = """
            SELECT $COLUMN_VALUE
              FROM $KEYSPACE.$TABLE_NAME
             WHERE $COLUMN_COUNTRY=?
               AND $COLUMN_PMD=?
               AND $COLUMN_OPERATION_TYPE=?
               AND $COLUMN_PARAMETER=?
        """
    }

    private val preparedFindByCountryPmdOperationType = session.prepare(FIND_BY_COUNTRY_PMD_OPERATION_TYPE_CQL)

    override fun findBy(
        country: String,
        pmd: Pmd,
        operationType: OperationType,
        parameter: String
    ): Result<String?, Fail> {

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
            ?.converter()
            ?.orForwardFail { error -> return error }
            .asSuccess()
    }

    private fun Row.converter(): Result<String, Fail> = this.getString(COLUMN_VALUE)
        .asSuccess()
}