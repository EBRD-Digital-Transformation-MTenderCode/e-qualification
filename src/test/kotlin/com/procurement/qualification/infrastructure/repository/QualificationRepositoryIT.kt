package com.procurement.qualification.infrastructure.repository

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
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.configuration.DatabaseTestConfiguration
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.QualificationEntity
import com.procurement.qualification.json.loadJson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DatabaseTestConfiguration::class])
class QualificationRepositoryIT {
    companion object {
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
        private val QUALIFICATION_PATH = "json/dto/qualification/qualification_full.json"
        private val QUALIFICATION_JSON = loadJson(QUALIFICATION_PATH)

        private const val KEYSPACE = "qualification"
        private const val TABLE_NAME = "qualification"
        private const val COLUMN_CPID = "cpid"
        private const val COLUMN_OCID = "ocid"
        private const val COLUMN_JSON_DATA = "json_data"
    }

    @Autowired
    private lateinit var container: CassandraTestContainer

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

        qualificationRepository = CassandraQualificationRepository(session)
    }

    @AfterEach
    fun clean() {
        dropKeyspace()
    }

    @Test
    fun findBy() {
        val qualification = createQualification()
        insertQualification(qualification)
        val actual = qualificationRepository.findBy(cpid = qualification.cpid, ocid = qualification.ocid).get

        val expectedList = listOf(qualification)

        assertFalse(actual.isEmpty())
        assertEquals(actual, expectedList)
    }

    @Test
    fun save() {
        val qualification = createQualification()
        qualificationRepository.save(qualification)
        val savedPeriod = qualificationRepository.findBy(cpid = qualification.cpid, ocid = qualification.ocid).get

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

        val actual = qualificationRepository.save(createQualification())

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
                        $COLUMN_JSON_DATA text,
                        primary key($COLUMN_CPID, $COLUMN_OCID)
                    );
            """
        )
    }

    private fun insertQualification(qualificationEntity: QualificationEntity) {
        val record = QueryBuilder.insertInto(KEYSPACE, TABLE_NAME)
            .value(COLUMN_CPID, qualificationEntity.cpid.toString())
            .value(COLUMN_OCID, qualificationEntity.ocid.toString())
            .value(COLUMN_JSON_DATA, qualificationEntity.jsonData)


        session.execute(record)
    }

    private fun createQualification() = QualificationEntity(
        cpid = CPID,
        ocid = OCID,
        jsonData = QUALIFICATION_JSON
    )
}