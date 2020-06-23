package com.procurement.qualification.infrastructure.repository

import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.procurement.qualification.application.repository.QualificationRepository
import com.procurement.qualification.domain.functional.MaybeFail
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.Result.Companion.failure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.infrastructure.extension.cassandra.tryExecute
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.QualificationEntity
import org.springframework.stereotype.Repository

@Repository
class CassandraQualificationRepository(private val session: Session) : QualificationRepository {

    companion object {
        private const val KEYSPACE = "qualification"
        private const val TABLE_NAME = "qualifications"
        private const val COLUMN_CPID = "cpid"
        private const val COLUMN_OCID = "ocid"
        private const val COLUMN_ID = "id"
        private const val COLUMN_JSON_DATA = "json_data"

        private const val SAVE_QUALIFICATION_CQL = """
               INSERT INTO $KEYSPACE.$TABLE_NAME(
                      $COLUMN_CPID,
                      $COLUMN_OCID,
                      $COLUMN_ID,
                      $COLUMN_JSON_DATA
               )
               VALUES(?, ?, ?, ?)
               IF NOT EXISTS
            """

        private const val UPDATE_QUALIFICATION_CQL = """
               UPDATE $KEYSPACE.$TABLE_NAME
                  SET $COLUMN_JSON_DATA=?
                WHERE $COLUMN_CPID=?
                  AND $COLUMN_OCID=?
                  AND $COLUMN_ID=?
            """

        private const val FIND_BY_CPID_AND_OCID_CQL = """
               SELECT $COLUMN_CPID,
                      $COLUMN_OCID,
                      $COLUMN_ID,
                      $COLUMN_JSON_DATA
                 FROM $KEYSPACE.$TABLE_NAME
                WHERE $COLUMN_CPID=? 
                  AND $COLUMN_OCID=?
            """

        private const val FIND_BY_CPID_AND_OCID_AND_ID_CQL = """
               SELECT $COLUMN_CPID,
                      $COLUMN_OCID,
                      $COLUMN_ID,
                      $COLUMN_JSON_DATA
                 FROM $KEYSPACE.$TABLE_NAME
                WHERE $COLUMN_CPID=? 
                  AND $COLUMN_OCID=?
                  AND $COLUMN_ID=?
            """
    }

    private val preparedFindByCpidAndOcidCQL = session.prepare(FIND_BY_CPID_AND_OCID_CQL)
    private val preparedSaveCQL = session.prepare(SAVE_QUALIFICATION_CQL)
    private val updateAll = session.prepare(UPDATE_QUALIFICATION_CQL)
    private val preparedFindByCpidAndOcidAndIdCQL = session.prepare(FIND_BY_CPID_AND_OCID_AND_ID_CQL)

    override fun findBy(cpid: Cpid, ocid: Ocid): Result<List<QualificationEntity>, Fail.Incident> {
        val query = preparedFindByCpidAndOcidCQL.bind()
            .apply {
                setString(COLUMN_CPID, cpid.toString())
                setString(COLUMN_OCID, ocid.toString())
            }

        return query.tryExecute(session)
            .orForwardFail { error -> return error }
            .map { row ->
                row.converter()
                    .orForwardFail { error -> return error }
            }
            .asSuccess()
    }

    override fun findBy(
        cpid: Cpid,
        ocid: Ocid,
        qualificationId: QualificationId
    ): Result<QualificationEntity?, Fail.Incident> {
        val query = preparedFindByCpidAndOcidAndIdCQL.bind()
            .apply {
                setString(COLUMN_CPID, cpid.toString())
                setString(COLUMN_OCID, ocid.toString())
                setString(COLUMN_ID, qualificationId.toString())
            }

        return query.tryExecute(session)
            .orForwardFail { error -> return error }
            .one()
            ?.converter()
            ?.orForwardFail { error -> return error }
            .asSuccess()
    }

    override fun save(entity: QualificationEntity): MaybeFail<Fail.Incident> {
        val statements = preparedSaveCQL.bind()
            .apply {
                setString(COLUMN_CPID, entity.cpid.toString())
                setString(COLUMN_OCID, entity.ocid.toString())
                setString(COLUMN_ID, entity.id.toString())
                setString(COLUMN_JSON_DATA, entity.jsonData)
            }

        statements.tryExecute(session)
            .doOnError { fail -> return MaybeFail.fail(fail) }

        return MaybeFail.none()
    }

    override fun saveAll(entities: List<QualificationEntity>): MaybeFail<Fail.Incident> {
        val statement = BatchStatement()

        entities.forEach { entity ->
            statement.add(
                preparedSaveCQL.bind()
                    .apply {
                        setString(COLUMN_CPID, entity.cpid.toString())
                        setString(COLUMN_OCID, entity.ocid.toString())
                        setString(COLUMN_ID, entity.id.toString())
                        setString(COLUMN_JSON_DATA, entity.jsonData)
                    }
            )
        }

        statement.tryExecute(session)
            .doOnError { fail -> return MaybeFail.fail(fail) }

        return MaybeFail.none()
    }

    override fun updateAll(entities: List<QualificationEntity>): MaybeFail<Fail.Incident> {
        val statement = BatchStatement()

        entities.forEach { entity ->
            statement.add(
                updateAll.bind()
                    .apply {
                        setString(COLUMN_CPID, entity.cpid.toString())
                        setString(COLUMN_OCID, entity.ocid.toString())
                        setString(COLUMN_ID, entity.id.toString())
                        setString(COLUMN_JSON_DATA, entity.jsonData)
                    }
            )
        }

        statement.tryExecute(session)
            .doOnError { fail -> return MaybeFail.fail(fail) }

        return MaybeFail.none()
    }

    private fun Row.converter(): Result<QualificationEntity, Fail.Incident> {
        val cpid = this.getString(COLUMN_CPID)
        val cpidParsed = Cpid.tryCreateOrNull(cpid)
            ?: return failure(
                Fail.Incident.Database.Parsing(
                    column = COLUMN_CPID, value = cpid
                )
            )

        val ocid = this.getString(COLUMN_OCID)
        val ocidParsed = Ocid.tryCreateOrNull(ocid)
            ?: return failure(
                Fail.Incident.Database.Parsing(
                    column = COLUMN_OCID, value = ocid
                )
            )
        val qualificationId = this.getString(COLUMN_ID)
        val idParsed = QualificationId.tryCreateOrNull(text = qualificationId)
            ?: return failure(
                Fail.Incident.Database.Parsing(
                    column = COLUMN_ID, value = qualificationId
                )
            )

        return QualificationEntity(
            cpid = cpidParsed,
            ocid = ocidParsed,
            id = idParsed,
            jsonData = this.getString(COLUMN_JSON_DATA)
        )
            .asSuccess()
    }
}
