package com.procurement.qualification.infrastructure.repository

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.HostDistance
import com.datastax.driver.core.PlainTextAuthProvider
import com.datastax.driver.core.PoolingOptions
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.nhaarman.mockito_kotlin.spy
import com.procurement.qualification.application.repository.QualificationStateRepository
import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.Pmd
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.infrastructure.configuration.DatabaseTestConfiguration
import com.procurement.qualification.infrastructure.model.entity.QualificationStateEntity
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
class QualificationStatesRepositoryIT {
    companion object {
        private const val COUNTRY = "country"
        private val PMD = Pmd.creator("GPA")
        private val OPERATION_TYPE = OperationType.creator("qualificationDeclareNonConflictOfInterest")
        private val STATUS = QualificationStatus.creator("unsuccessful")
        private val STATUS_DETAILS = QualificationStatusDetails.creator("consideration")

        private const val KEYSPACE = "qualification"
        private const val TABLE_NAME = "qualification_states"
        private const val COLUMN_COUNTRY = "country"
        private const val COLUMN_PMD = "pmd"
        private const val COLUMN_OPERATION_TYPE = "operationType"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_STATUS_DETAILS = "status_details"
    }

    @Autowired
    private lateinit var container: CassandraTestContainer

    private lateinit var session: Session
    private lateinit var qualificationStatesRepository: QualificationStateRepository

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

        qualificationStatesRepository = CassandraQualificationStateRepository(session)
    }

    @AfterEach
    fun clean() {
        dropKeyspace()
    }

    @Test
    fun findBy() {
        val qualificationState = createQualification()
        insertQualificationState(qualificationState)
        val actual = qualificationStatesRepository.findBy(
            country = COUNTRY,
            operationType = OPERATION_TYPE,
            pmd = PMD
        ).get

        val expectedList = listOf(qualificationState)

        assertFalse(actual.isEmpty())
        assertEquals(actual, expectedList)
    }

    @Test
    fun entityNotFound() {
        val actual = qualificationStatesRepository.findBy(
            country = COUNTRY,
            operationType = OPERATION_TYPE,
            pmd = PMD
        ).get

        assertTrue(actual.isEmpty())
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
                        $COLUMN_COUNTRY text,
                        $COLUMN_PMD text,
                        $COLUMN_OPERATION_TYPE text,
                        $COLUMN_STATUS text,
                        $COLUMN_STATUS_DETAILS text,
                        primary key($COLUMN_COUNTRY, $COLUMN_PMD, $COLUMN_OPERATION_TYPE)
                    );
            """
        )
    }

    private fun insertQualificationState(qualificationStateEntity: QualificationStateEntity) {
        val record = QueryBuilder.insertInto(KEYSPACE, TABLE_NAME)
            .value(COLUMN_COUNTRY, qualificationStateEntity.country)
            .value(COLUMN_PMD, qualificationStateEntity.pmd.toString())
            .value(COLUMN_OPERATION_TYPE, qualificationStateEntity.operationType.toString())
            .value(COLUMN_STATUS, qualificationStateEntity.status.toString())
            .value(COLUMN_STATUS_DETAILS, qualificationStateEntity.statusDetails.toString())


        session.execute(record)
    }

    private fun createQualification() = QualificationStateEntity(
        country = COUNTRY, pmd = PMD, operationType = OPERATION_TYPE, status = STATUS, statusDetails = STATUS_DETAILS
    )
}
