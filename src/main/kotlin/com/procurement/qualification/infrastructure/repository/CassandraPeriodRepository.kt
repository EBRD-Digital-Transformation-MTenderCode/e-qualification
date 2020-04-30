package com.procurement.qualification.infrastructure.repository

import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.procurement.qualification.application.repository.PeriodRepository
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.Result.Companion.failure
import com.procurement.qualification.domain.functional.Result.Companion.success
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.functional.bind
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.extension.cassandra.tryExecute
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.PeriodEntity
import com.procurement.qualification.infrastructure.utils.toDate
import com.procurement.qualification.infrastructure.utils.toLocal
import org.springframework.stereotype.Repository

@Repository
class CassandraPeriodRepository(private val session: Session) : PeriodRepository {

    companion object {
        private const val keySpace = "qualification"
        private const val tableName = "period"
        private const val columnCpid = "cpid"
        private const val columnOcid = "ocid"
        private const val columnStartDate = "start_date"
        private const val columnEndDate = "end_date"

        private const val SAVE_NEW_PERIOD = """
               INSERT INTO $keySpace.$tableName(
                      $columnCpid,
                      $columnOcid,
                      $columnStartDate,
                      $columnEndDate
               )
               VALUES(?, ?, ?, ?)
               IF NOT EXISTS
            """

        private const val FIND_BY_CPID_AND_OCID_CQL = """
               SELECT $columnCpid,
                      $columnOcid,
                      $columnStartDate,
                      $columnEndDate
                 FROM $keySpace.$tableName
                WHERE $columnCpid=? 
                  AND $columnOcid=?
            """
    }

    private val preparedFindByCpidAndOcidCQL = session.prepare(FIND_BY_CPID_AND_OCID_CQL)
    private val preparedSaveNewPeriodCQL = session.prepare(SAVE_NEW_PERIOD)

    override fun findBy(cpid: Cpid, ocid: Ocid): Result<PeriodEntity?, Fail.Incident> {
        val query = preparedFindByCpidAndOcidCQL.bind()
            .apply {
                setString(columnCpid, cpid.toString())
                setString(columnOcid, ocid.toString())
            }

        return query.tryExecute(session)
            .orForwardFail { error -> return error }
            .one()
            ?.let { row -> converter(row = row) }
            ?.orForwardFail { error -> return error }
            .asSuccess()
    }

    private fun converter(row: Row): Result<PeriodEntity, Fail.Incident> {
        val cpid = row.getString(columnCpid)
        val cpidParsed = Cpid.tryCreateOrNull(cpid)
            ?: return failure(
                Fail.Incident.Database.ParseFromDatabaseColumnIncident(
                    column = columnCpid, value = cpid
                )
            )

        val ocid = row.getString(columnOcid)
        val ocidParsed = Ocid.tryCreateOrNull(ocid)
            ?: return failure(
                Fail.Incident.Database.ParseFromDatabaseColumnIncident(
                    column = columnOcid, value = ocid
                )
            )

        return PeriodEntity(
            cpid = cpidParsed,
            ocid = ocidParsed,
            endDate = row.getTimestamp(columnEndDate).toLocal(),
            startDate = row.getTimestamp(columnStartDate).toLocal()
        ).asSuccess()
    }

    override fun saveNewPeriod(period: PeriodEntity): Result<Boolean, Fail.Incident> {
        val statements = preparedSaveNewPeriodCQL.bind()
            .apply {
                setString(columnCpid, period.cpid.toString())
                setString(columnOcid, period.ocid.toString())
                setTimestamp(columnStartDate, period.startDate.toDate())
                setTimestamp(columnEndDate, period.endDate.toDate())
            }

        return statements.tryExecute(session).bind { resultSet ->
            success(resultSet.wasApplied())
        }
    }
}
