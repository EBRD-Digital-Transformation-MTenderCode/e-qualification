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
import com.procurement.qualification.application.repository.PeriodRepository
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.bind.databinding.JsonDateTimeDeserializer
import com.procurement.qualification.infrastructure.bind.databinding.JsonDateTimeSerializer
import com.procurement.qualification.infrastructure.configuration.DatabaseTestConfiguration
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.PeriodEntity
import com.procurement.qualification.infrastructure.utils.toDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DatabaseTestConfiguration::class])
class PeriodDataEntityRepositoryIT {
    companion object {
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
        private val START_DATE = JsonDateTimeDeserializer.deserialize(JsonDateTimeSerializer.serialize(LocalDateTime.now()))
        private val END_DATE = START_DATE.plusDays(2)

        private const val KEYSPACE = "qualification"
        private const val TABLE_NAME = "period"
        private const val COLUMN_CPID = "cpid"
        private const val COLUMN_OCID = "ocid"
        private const val COLUMN_START_DATE = "start_date"
        private const val COLUMN_END_DATE = "end_date"
    }

    @Autowired
    private lateinit var container: CassandraTestContainer

    private lateinit var session: Session
    private lateinit var periodRepository: PeriodRepository

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

        periodRepository = CassandraPeriodRepository(session)
    }

    @AfterEach
    fun clean() {
        dropKeyspace()
    }

    @Test
    fun findBy() {
        val period = stubPeriod()
        insertPeriod(period)
        val actualPeriod = periodRepository.findBy(cpid = period.cpid, ocid = period.ocid).get

        assertEquals(actualPeriod, period)
    }

    @Test
    fun saveNewPeriod() {
        val period = stubPeriod()
        periodRepository.saveNewPeriod(period)
        val savedPeriod = periodRepository.findBy(cpid = period.cpid, ocid = period.ocid).get

        assertEquals(savedPeriod, period)
    }

    @Test
    fun cnNotFound() {
        val actualPeriod = periodRepository.findBy(
            cpid = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033000")!!,
            ocid = OCID
        ).get
        assertTrue(actualPeriod == null)
    }

    @Test
    fun `error while saving`() {
        doThrow(RuntimeException())
            .whenever(session)
            .execute(any<BoundStatement>())

        val actual = periodRepository.saveNewPeriod(stubPeriod())

        assertTrue(actual.isFail)
        assertTrue(actual.error is Fail.Incident.Database.DatabaseInteractionIncident)
    }

    @Test
    fun `error while finding`() {
        doThrow(RuntimeException())
            .whenever(session)
            .execute(any<BoundStatement>())

        val actual = periodRepository.findBy(CPID, OCID)

        assertTrue(actual.isFail)
        assertTrue(actual.error is Fail.Incident.Database.DatabaseInteractionIncident)
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
                CREATE TABLE IF NOT EXISTS qualification.period
                    (
                        cpid text,
                        ocid text,
                        start_date timestamp,
                        end_date timestamp,
                        primary key(cpid, ocid)
                    );
            """
        )
    }

    private fun insertPeriod(periodEntity: PeriodEntity) {
        val record = QueryBuilder.insertInto(KEYSPACE, TABLE_NAME)
            .value(COLUMN_CPID, periodEntity.cpid.toString())
            .value(COLUMN_OCID, periodEntity.ocid.toString())
            .value(COLUMN_START_DATE, periodEntity.startDate.toDate())
            .value(COLUMN_END_DATE, periodEntity.endDate.toDate())
        session.execute(record)
    }

    private fun stubPeriod() = PeriodEntity(
        cpid = CPID,
        ocid = OCID,
        startDate = START_DATE,
        endDate = END_DATE
    )
}
