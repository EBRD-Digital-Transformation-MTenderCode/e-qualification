package com.procurement.qualification.infrastructure.repository

import com.datastax.driver.core.Session
import com.procurement.qualification.application.repository.PeriodRulesRepository
import com.procurement.qualification.domain.enums.ProcurementMethod
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.infrastructure.extension.cassandra.tryExecute
import com.procurement.qualification.infrastructure.fail.Fail
import org.springframework.stereotype.Repository

@Repository
class CassandraPeriodRulesRepository(private val session: Session) : PeriodRulesRepository {
    companion object {
        private const val keySpace = "qualification"
        private const val tableName = "period_rules"
        private const val columnCountry = "country"
        private const val columnPmd = "pmd"
        private const val columnValue = "value"

        private const val FIND_BY_COUNTRY_AND_PMD_CQL = """
               SELECT $columnValue
                 FROM $keySpace.$tableName
                WHERE $columnCountry=? 
                  AND $columnPmd=?
            """
    }

    private val preparedFindPeriodRuleCQL = session.prepare(FIND_BY_COUNTRY_AND_PMD_CQL)

    override fun findTermBy(country: String, pmd: ProcurementMethod): Result<Long?, Fail.Incident> {
        val query = preparedFindPeriodRuleCQL.bind()
            .apply {
                setString(columnCountry, country)
                setString(columnPmd, pmd.name)
            }

        return query.tryExecute(session)
            .orForwardFail { incident -> return incident }
            .one()
            ?.getLong(columnValue)
            .asSuccess()
    }
}