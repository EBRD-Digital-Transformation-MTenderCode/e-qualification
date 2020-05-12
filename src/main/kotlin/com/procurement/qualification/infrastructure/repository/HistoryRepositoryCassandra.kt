package com.procurement.qualification.infrastructure.repository

import com.datastax.driver.core.Session
import com.procurement.qualification.application.repository.HistoryRepository
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.util.extension.nowDefaultUTC
import com.procurement.qualification.domain.util.extension.toDate
import com.procurement.qualification.infrastructure.extension.cassandra.tryExecute
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.HistoryEntity
import com.procurement.qualification.infrastructure.utils.toJson
import org.springframework.stereotype.Repository

@Repository
class HistoryRepositoryCassandra(private val session: Session) : HistoryRepository {

    companion object {
        private const val KEYSPACE = "qualification"
        private const val HISTORY_TABLE = "history"
        private const val COMMAND_ID = "command_id"
        private const val COMMAND = "command"
        private const val COMMAND_DATE = "command_date"
        private const val JSON_DATA = "json_data"

        private const val SAVE_HISTORY_CQL = """
               INSERT INTO $KEYSPACE.$HISTORY_TABLE(
                      $COMMAND_ID,
                      $COMMAND,
                      $COMMAND_DATE,
                      $JSON_DATA
               )
               VALUES(?, ?, ?, ?)
               IF NOT EXISTS
            """

        private const val FIND_HISTORY_ENTRY_CQL = """
               SELECT $COMMAND_ID,
                      $COMMAND,
                      $COMMAND_DATE,
                      $JSON_DATA
                 FROM $KEYSPACE.$HISTORY_TABLE
                WHERE $COMMAND_ID=?
                  AND $COMMAND=?
               LIMIT 1
            """
    }

    private val preparedSaveHistoryCQL = session.prepare(SAVE_HISTORY_CQL)
    private val preparedFindHistoryByCpidAndCommandCQL = session.prepare(FIND_HISTORY_ENTRY_CQL)

    override fun getHistory(operationId: String, command: String): Result<HistoryEntity?, Fail.Incident> {
        val query = preparedFindHistoryByCpidAndCommandCQL.bind()
            .apply {
                setString(COMMAND_ID, operationId)
                setString(COMMAND, command)
            }

        return query.tryExecute(session)
            .doOnError { error -> return Result.failure(error) }
            .get
            .one()
            ?.let { row ->
                HistoryEntity(
                    row.getString(COMMAND_ID),
                    row.getString(COMMAND),
                    row.getTimestamp(COMMAND_DATE),
                    row.getString(JSON_DATA)
                )
            }
            .asSuccess()
    }

    override fun saveHistory(operationId: String, command: String, result: Any): Result<HistoryEntity, Fail.Incident> {
        val entity = HistoryEntity(
            operationId = operationId,
            command = command,
            operationDate = nowDefaultUTC().toDate(),
            jsonData = result.toJson()
        )

        val insert = preparedSaveHistoryCQL.bind()
            .apply {
                setString(COMMAND_ID, entity.operationId)
                setString(COMMAND, entity.command)
                setTimestamp(COMMAND_DATE, entity.operationDate)
                setString(JSON_DATA, entity.jsonData)
            }

        insert.tryExecute(session)
            .doOnError { error -> return Result.failure(error) }

        return entity.asSuccess()
    }
}
