package com.procurement.qualification.infrastructure.repository

import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.HostDistance
import com.datastax.driver.core.PlainTextAuthProvider
import com.datastax.driver.core.PoolingOptions
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.qualification.application.repository.QualificationRepository
import com.procurement.qualification.application.service.Transform
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.measure.Scoring
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.submission.SubmissionId
import com.procurement.qualification.infrastructure.bind.databinding.JsonDateTimeDeserializer
import com.procurement.qualification.infrastructure.bind.databinding.JsonDateTimeSerializer
import com.procurement.qualification.infrastructure.configuration.DatabaseTestConfiguration
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.QualificationEntity
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DatabaseTestConfiguration::class])
class QualificationRepositoryIT {
    companion object {
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
        private val DATE = JsonDateTimeDeserializer.deserialize(JsonDateTimeSerializer.serialize(LocalDateTime.now()))

        private const val KEYSPACE = "qualification"
        private const val TABLE_NAME = "qualifications"
        private const val COLUMN_CPID = "cpid"
        private const val COLUMN_OCID = "ocid"
        private const val COLUMN_ID = "id"
        private const val COLUMN_JSON_DATA = "json_data"
    }

    @Autowired
    private lateinit var container: CassandraTestContainer

    @Autowired
    private lateinit var transform: Transform

    private lateinit var session: Session
    private lateinit var qualificationRepository: QualificationRepository

    @BeforeEach
    fun init() {
        val poolingOptions = PoolingOptions()
            .setMaxConnectionsPerHost(HostDistance.LOCAL, 1)
        val cluster = Cluster.builder()
            .addContactPoints(container.contractPoint)
            .withPort(container.port)
            .withoutJMXReporting()
            .withPoolingOptions(poolingOptions)
            .withAuthProvider(PlainTextAuthProvider(container.username, container.password))
            .build()

        session = spy(cluster.connect())

        createKeyspace()
        createTable()

        qualificationRepository = CassandraQualificationRepository(session, transform)
    }

    @AfterEach
    fun clean() {
        dropKeyspace()
    }

    @Test
    fun findBy() {
        val qualification = createQualification()
        insertQualification(qualification)
        val actual = qualificationRepository.findBy(cpid = CPID, ocid = OCID).get

        val expectedList = listOf(qualification)

        assertEquals(actual, expectedList)
    }

    @Test
    fun verifyThatFindByIsSuccess() {
        val expected = createQualification()
        insertQualification(expected)
        val actual = qualificationRepository.findBy(
            cpid = CPID,
            ocid = OCID,
            qualificationId = expected.id
        ).get

        assertNotNull(actual)
        assertEquals(actual, expected)
    }

    @Test
    fun verifyThatFindByIsNotFound() {
        val expected = createQualification()
        val actual = qualificationRepository.findBy(
            cpid = CPID,
            ocid = OCID,
            qualificationId = expected.id
        ).get

        assertNull(actual)
    }

    @Test
    fun verifyThatFindByIsError() {
        val expected = createQualification()
        doThrow(RuntimeException())
            .whenever(session)
            .execute(any<BoundStatement>())

        val actual = qualificationRepository.findBy(
            cpid = CPID,
            ocid = OCID,
            qualificationId = expected.id
        )

        assertTrue(actual.isFail)
        assertTrue(actual.error is Fail.Incident.Database.Interaction)
    }

    @Test
    fun save() {
        val qualification = createQualification()
        qualificationRepository.add(CPID, OCID, qualification)
        val savedPeriod = qualificationRepository.findBy(CPID, OCID).get

        val expectedList = listOf(qualification)
        assertEquals(savedPeriod, expectedList)
    }

    @Test
    fun entityNotFound() {
        val actual = qualificationRepository.findBy(cpid = CPID, ocid = OCID).get
        assertTrue(actual.isEmpty())
    }

    @Test
    fun `error while saving`() {
        doThrow(RuntimeException())
            .whenever(session)
            .execute(any<BoundStatement>())

        val actual = qualificationRepository.add(CPID, OCID, createQualification())

        assertTrue(actual.isFail)
        assertTrue(actual.error is Fail.Incident.Database.Interaction)
    }

    @Test
    fun saveAll() {
        val qual1 = createQualification()
        val qual2 = createQualification()
        val expectedQualifications = listOf(qual1, qual2)
        qualificationRepository.add(CPID, OCID, expectedQualifications)
        val savedQualifications = qualificationRepository.findBy(cpid = CPID, ocid = OCID).get
        expectedQualifications.forEach { expected ->
            val actualQualification = savedQualifications.find { it.id == expected.id }!!
            assertEquals(expected, actualQualification)
        }
    }

    @Test
    fun updateAll() {
        val qual1 = createQualification()
        val qual2 = createQualification()
        insertQualification(qual1)
        insertQualification(qual2)
        val updatedQualifications = listOf(
            qual1.copy(statusDetails = QualificationStatusDetails.UNSUCCESSFUL),
            qual2.copy(relatedSubmission = SubmissionId.generate())
        )
        qualificationRepository.update(CPID, OCID, updatedQualifications)
        val updated = qualificationRepository.findBy(cpid = CPID, ocid = OCID).get
        updated.forEach { expected ->
            val actualQualification = updatedQualifications.find { it.id == expected.id }!!
            assertEquals(expected, actualQualification)
        }
    }

    @Test
    fun findAll_success() {
        val qual1 = createQualification()
        val qual2 = createQualification()
        insertQualification(qual1)
        insertQualification(qual2)
        val qualifications = qualificationRepository.findBy(
            cpid = CPID, ocid = OCID, qualificationIds = listOf(qual1.id, qual2.id)
        ).get
        val uniqueQualifications = qualifications.toSet()
        val expected = setOf(qual1, qual2)

        assertEquals(qualifications.size, uniqueQualifications.size)
        assertEquals(expected, uniqueQualifications)
    }

    @Test
    fun findAll_noQualificationFound_success() {
        val qualification = createQualification()

        val actual = qualificationRepository.findBy(
            cpid = CPID, ocid = OCID, qualificationIds = listOf(qualification.id)
        ).get

        assertTrue(actual.isEmpty())
    }

    @Test
    fun findAll_error() {
        doThrow(RuntimeException())
            .whenever(session)
            .execute(any<BoundStatement>())

        val qualification = createQualification()

        val actual = qualificationRepository.findBy(
            cpid = CPID, ocid = OCID, qualificationIds = listOf(qualification.id)
        )

        assertTrue(actual.isFail)
        assertTrue(actual.error is Fail.Incident.Database.Interaction)
    }

    @Test
    fun `error while saving all`() {
        doThrow(RuntimeException())
            .whenever(session)
            .execute(any<BatchStatement>())

        val actual = qualificationRepository.add(CPID, OCID, listOf(createQualification()))

        assertTrue(actual.isFail)
        assertTrue(actual.error is Fail.Incident.Database.Interaction)
    }

    private fun createKeyspace() {
        session.execute(
            "CREATE KEYSPACE $KEYSPACE " +
                "WITH replication = {'class' : 'SimpleStrategy', 'replication_factor' : 1};"
        )
    }

    private fun dropKeyspace() {
        session.execute("DROP KEYSPACE $KEYSPACE;")
    }

    private fun createTable() {
        session.execute(
            """
                CREATE TABLE IF NOT EXISTS $KEYSPACE.$TABLE_NAME
                    (
                        $COLUMN_CPID text,
                        $COLUMN_OCID text,
                        $COLUMN_ID text,
                        $COLUMN_JSON_DATA text,
                        primary key($COLUMN_CPID, $COLUMN_OCID, $COLUMN_ID)
                    );
            """
        )
    }

    private fun insertQualification(qualification: Qualification) {
        val record = QueryBuilder.insertInto(KEYSPACE, TABLE_NAME)
            .value(COLUMN_CPID, CPID.toString())
            .value(COLUMN_OCID, OCID.toString())
            .value(COLUMN_ID, qualification.id.toString())
            .value(COLUMN_JSON_DATA, generateJsonData(qualification))


        session.execute(record)
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

    private fun generateJsonData(qualification: Qualification): String {
        val entity = convert(qualification)
        return transform.trySerialization(entity).get
    }

    private fun createQualification() = Qualification(
        id = QualificationId.generate(),
        date = DATE,
        owner = UUID.randomUUID(),
        token = UUID.randomUUID(),
        status = QualificationStatus.ACTIVE,
        scoring = Scoring.tryCreate("0.001").get,
        statusDetails = QualificationStatusDetails.ACTIVE,
        relatedSubmission = SubmissionId.generate()
    )
}
