package com.procurement.qualification.infrastructure.repository

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.HostDistance
import com.datastax.driver.core.PlainTextAuthProvider
import com.datastax.driver.core.PoolingOptions
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.nhaarman.mockito_kotlin.spy
import com.procurement.qualification.application.repository.QualificationRulesRepository
import com.procurement.qualification.domain.enums.OperationType
import com.procurement.qualification.domain.enums.ProcurementMethodDetails
import com.procurement.qualification.infrastructure.configuration.DatabaseTestConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DatabaseTestConfiguration::class])
class QualificationRulesRepositoryIT {
    companion object {
        private const val COUNTRY = "country"
        private val PMD = ProcurementMethodDetails.creator("GPA")
        private val OPERATION_TYPE = OperationType.creator("qualificationDeclareNonConflictOfInterest")
        private const val VALUE = "some data"

        private const val KEYSPACE = "qualification"
        private const val TABLE_NAME = "qualification_rules"
        private const val COLUMN_COUNTRY = "country"
        private const val COLUMN_PMD = "pmd"
        private const val COLUMN_OPERATION_TYPE = "operation_type"
        private const val COLUMN_PARAMETER = "parameter"
        private const val COLUMN_VALUE = "value"

        private const val VALID_STATES_PARAMETER = "validStates"
    }

    @Autowired
    private lateinit var container: CassandraTestContainer

    private lateinit var session: Session
    private lateinit var qualificationStatesRepository: QualificationRulesRepository

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

        qualificationStatesRepository = CassandraQualificationRulesRepository(session)
    }

    @AfterEach
    fun clean() {
        dropKeyspace()
    }

    @Test
    fun findBy() {
        insertQualificationState()
        val actual = qualificationStatesRepository.findBy(
            country = COUNTRY,
            operationType = OPERATION_TYPE,
            pmd = PMD,
            parameter = VALID_STATES_PARAMETER
        ).get

        assertNotNull(actual)
        assertEquals(VALUE, actual)
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
                        $COLUMN_PARAMETER text,
                        $COLUMN_VALUE text,
                        primary key($COLUMN_COUNTRY, $COLUMN_PMD, $COLUMN_OPERATION_TYPE, $COLUMN_PARAMETER)
                    );
            """
        )
    }

    private fun insertQualificationState() {
        val record = QueryBuilder.insertInto(KEYSPACE, TABLE_NAME)
            .value(COLUMN_COUNTRY, COUNTRY)
            .value(COLUMN_PMD, PMD.toString())
            .value(COLUMN_OPERATION_TYPE, OPERATION_TYPE.toString())
            .value(COLUMN_PARAMETER, VALID_STATES_PARAMETER)
            .value(COLUMN_VALUE, VALUE)

        session.execute(record)
    }
}
