package com.procurement.qualification.infrastructure.repository

import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.procurement.qualification.application.repository.QualificationStateRepository
import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.Pmd
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails
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
        private const val COLUMN_OPERATION_TYPE = "operationType"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_STATUS_DETAILS = "status_details"

        private const val FIND_BY_COUNTRY_PMD_OPERATION_TYPE_CQL = """
            SELECT $COLUMN_COUNTRY,
                   $COLUMN_PMD,
                   $COLUMN_OPERATION_TYPE,
                   $COLUMN_STATUS,
                   $COLUMN_STATUS_DETAILS
              FROM $KEYSPACE.$TABLE_NAME
             WHERE $COLUMN_COUNTRY=?
               AND $COLUMN_PMD=?
               AND $COLUMN_OPERATION_TYPE=?
        """
    }

    private val preparedFindByCountryPmdOperationType = session.prepare(FIND_BY_COUNTRY_PMD_OPERATION_TYPE_CQL)

    override fun findBy(
        country: String,
        pmd: Pmd,
        operationType: OperationType
    ): Result<List<QualificationStateEntity>, Fail> {
        val query = preparedFindByCountryPmdOperationType.bind()
            .apply {
                setString(COLUMN_COUNTRY, country)
                setString(COLUMN_PMD, pmd.toString())
                setString(COLUMN_OPERATION_TYPE, operationType.toString())
            }

        return query.tryExecute(session)
            .orForwardFail { error -> return error }
            .map { row ->
                converter(row = row)
                    .orForwardFail { error -> return error }
            }
            .asSuccess()
    }

    private fun converter(row: Row): Result<QualificationStateEntity, Fail> {
        val country = row.getString(COLUMN_COUNTRY)
        val pmd = row.getString(COLUMN_PMD)
        val pmdParsed = Pmd.orNull(pmd)
            ?: return Result.failure(Fail.Incident.Database.Parsing(column = COLUMN_PMD, value = pmd))

        val operationType = row.getString(COLUMN_OPERATION_TYPE)
        val operationTypeParsed = OperationType.orNull(operationType)
            ?: return Result.failure(
                Fail.Incident.Database.Parsing(
                    column = COLUMN_OPERATION_TYPE,
                    value = operationType
                )
            )

        val status = row.getString(COLUMN_STATUS)
        val statusParsed = QualificationStatus.orNull(status)
            ?: return Result.failure(
                Fail.Incident.Database.Parsing(
                    column = COLUMN_STATUS,
                    value = status
                )
            )

        val statusDetails = row.getString(COLUMN_STATUS_DETAILS)
        val statusDetailsParsed = QualificationStatusDetails.orNull(statusDetails)
            ?: return Result.failure(
                Fail.Incident.Database.Parsing(
                    column = COLUMN_STATUS_DETAILS,
                    value = statusDetails
                )
            )
        return QualificationStateEntity(
            country = country,
            pmd = pmdParsed,
            operationType = operationTypeParsed,
            status = statusParsed,
            statusDetails = statusDetailsParsed
        )
            .asSuccess()
    }
}