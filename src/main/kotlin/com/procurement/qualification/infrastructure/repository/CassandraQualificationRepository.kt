package com.procurement.qualification.infrastructure.repository

import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.procurement.qualification.application.repository.QualificationRepository
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.domain.functional.MaybeFail
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.infrastructure.extension.cassandra.tryExecute
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.QualificationEntity
import org.springframework.stereotype.Repository

@Repository
class CassandraQualificationRepository(
    private val session: Session,
    val transform: Transform
) : QualificationRepository {

    companion object {
        private const val KEYSPACE = "qualification"
        private const val TABLE_NAME = "qualifications"
        private const val COLUMN_CPID = "cpid"
        private const val COLUMN_OCID = "ocid"
        private const val COLUMN_ID = "id"
        private const val COLUMN_JSON_DATA = "json_data"
        private const val ID_VALUES = "id_values"

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

        private const val FIND_BY_CPID_OCID_AND_IDS_CQL = """
               SELECT $COLUMN_CPID,
                      $COLUMN_OCID,
                      $COLUMN_ID,
                      $COLUMN_JSON_DATA
                 FROM $KEYSPACE.$TABLE_NAME
                WHERE $COLUMN_CPID=? 
                  AND $COLUMN_OCID=?
                  AND $COLUMN_ID IN :$ID_VALUES;
            """
    }

    private val preparedFindByCpidAndOcidCQL = session.prepare(FIND_BY_CPID_AND_OCID_CQL)
    private val preparedSaveCQL = session.prepare(SAVE_QUALIFICATION_CQL)
    private val updateAll = session.prepare(UPDATE_QUALIFICATION_CQL)
    private val preparedFindByCpidAndOcidAndIdCQL = session.prepare(FIND_BY_CPID_AND_OCID_AND_ID_CQL)
    private val preparedFindByCpidOcidAndIdsCQL = session.prepare(FIND_BY_CPID_OCID_AND_IDS_CQL)

    override fun findBy(cpid: Cpid, ocid: Ocid): Result<List<Qualification>, Fail.Incident> {
        val query = preparedFindByCpidAndOcidCQL.bind()
            .apply {
                setString(COLUMN_CPID, cpid.toString())
                setString(COLUMN_OCID, ocid.toString())
            }

        return query.tryExecute(session)
            .orForwardFail { error -> return error }
            .map { row ->
                row.toQualification()
                    .orForwardFail { error -> return error }
            }
            .asSuccess()
    }

    override fun findBy(
        cpid: Cpid,
        ocid: Ocid,
        qualificationId: QualificationId
    ): Result<Qualification?, Fail.Incident> {
        val query = preparedFindByCpidAndOcidAndIdCQL.bind()
            .apply {
                setString(COLUMN_CPID, cpid.toString())
                setString(COLUMN_OCID, ocid.toString())
                setString(COLUMN_ID, qualificationId.toString())
            }

        return query.tryExecute(session)
            .orForwardFail { error -> return error }
            .one()
            ?.toQualification()
            ?.orForwardFail { error -> return error }
            .asSuccess()
    }

    override fun findBy(
        cpid: Cpid,
        ocid: Ocid,
        qualificationIds: List<QualificationId>
    ): Result<List<Qualification>, Fail.Incident> {
        val query = preparedFindByCpidOcidAndIdsCQL.bind()
            .setList(ID_VALUES, qualificationIds.map { it.toString() })
            .setString(COLUMN_CPID, cpid.toString())
            .setString(COLUMN_OCID, ocid.toString())

        return query.tryExecute(session)
            .orForwardFail { error -> return error }
            .map { row ->
                row.toQualification()
                    .orForwardFail { fail -> return fail }
            }
            .asSuccess()
    }

    override fun save(cpid: Cpid, ocid: Ocid, qualification: Qualification): MaybeFail<Fail.Incident> {
        val data = generateJsonData(qualification)
            .doReturn { fail -> return MaybeFail.fail(fail) }

        val statements = preparedSaveCQL.bind()
            .apply {
                setString(COLUMN_CPID, cpid.toString())
                setString(COLUMN_OCID, ocid.toString())
                setString(COLUMN_ID, qualification.id.toString())
                setString(COLUMN_JSON_DATA, data)
            }

        statements.tryExecute(session)
            .doOnError { fail -> return MaybeFail.fail(fail) }

        return MaybeFail.none()
    }

    override fun saveAll(cpid: Cpid, ocid: Ocid, qualifications: List<Qualification>): MaybeFail<Fail.Incident> {
        val statement = BatchStatement()

        qualifications.forEach { qualification ->
            val data = generateJsonData(qualification)
                .doReturn { fail -> return MaybeFail.fail(fail) }

            statement.add(
                preparedSaveCQL.bind()
                    .apply {
                        setString(COLUMN_CPID, cpid.toString())
                        setString(COLUMN_OCID, ocid.toString())
                        setString(COLUMN_ID, qualification.id.toString())
                        setString(COLUMN_JSON_DATA, data)
                    }
            )
        }

        statement.tryExecute(session)
            .doOnError { fail -> return MaybeFail.fail(fail) }

        return MaybeFail.none()
    }

    override fun updateAll(cpid: Cpid, ocid: Ocid, qualifications: List<Qualification>): MaybeFail<Fail.Incident> {
        val statement = BatchStatement()

        qualifications.forEach { qualification ->
            val data = generateJsonData(qualification)
                .doReturn { fail -> return MaybeFail.fail(fail) }

            statement.add(
                updateAll.bind()
                    .apply {
                        setString(COLUMN_CPID, cpid.toString())
                        setString(COLUMN_OCID, ocid.toString())
                        setString(COLUMN_ID, qualification.id.toString())
                        setString(COLUMN_JSON_DATA, data)
                    }
            )
        }

        statement.tryExecute(session)
            .doOnError { fail -> return MaybeFail.fail(fail) }

        return MaybeFail.none()
    }

    private fun Row.toQualification(): Result<Qualification, Fail.Incident> {
        val data = getString(COLUMN_JSON_DATA)
        val entity = transform.tryDeserialization(value = data, target = QualificationEntity::class.java)
            .doReturn { fail ->
                return Fail.Incident.Database.DatabaseParsing(exception = fail.exception).asFailure()
            }

        return Qualification(
            id = entity.id,
            date = entity.date,
            owner = entity.owner,
            token = entity.token,
            status = entity.status,
            statusDetails = entity.statusDetails,
            scoring = entity.scoring,
            relatedSubmission = entity.relatedSubmission,
            requirementResponses = entity.requirementResponses.map { requirementResponse ->
                Qualification.RequirementResponse(
                    id = requirementResponse.id,
                    value = requirementResponse.value,
                    responder = requirementResponse.responder.let { responder ->
                        Qualification.RequirementResponse.Responder(
                            id = responder.id,
                            name = responder.name
                        )
                    },
                    requirement = Qualification.RequirementResponse.Requirement(requirementResponse.requirement.id),
                    relatedTenderer = Qualification.RequirementResponse.RelatedTenderer(id = requirementResponse.relatedTenderer.id)
                )
            }
        ).asSuccess()
    }

    private fun convert(qualification: Qualification) = QualificationEntity(
        id = qualification.id,
        date = qualification.date,
        owner = qualification.owner,
        token = qualification.token,
        status = qualification.status,
        statusDetails = qualification.statusDetails,
        scoring = qualification.scoring,
        relatedSubmission = qualification.relatedSubmission,
        requirementResponses = qualification.requirementResponses.map { requirementResponse ->
            QualificationEntity.RequirementResponse(
                id = requirementResponse.id,
                value = requirementResponse.value,
                responder = requirementResponse.responder.let { responder ->
                    QualificationEntity.RequirementResponse.Responder(
                        id = responder.id,
                        name = responder.name
                    )
                },
                requirement = QualificationEntity.RequirementResponse.Requirement(requirementResponse.requirement.id),
                relatedTenderer = QualificationEntity.RequirementResponse.RelatedTenderer(id = requirementResponse.relatedTenderer.id)
            )
        }
    )

    private fun generateJsonData(qualification: Qualification): Result<String, Fail.Incident>{
        val entity = convert(qualification)
        return transform.trySerialization(entity)
            .doOnError { error -> return error.asFailure() }

    }

}
