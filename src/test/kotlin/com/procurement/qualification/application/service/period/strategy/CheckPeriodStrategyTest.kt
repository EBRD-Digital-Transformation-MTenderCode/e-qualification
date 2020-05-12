package com.procurement.qualification.application.service.period.strategy

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.qualification.application.model.period.check.CheckPeriodContext
import com.procurement.qualification.application.model.period.check.CheckPeriodData
import com.procurement.qualification.application.model.period.check.CheckPeriodResult
import com.procurement.qualification.application.repository.PeriodRepository
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.fail.error.ValidationError
import com.procurement.qualification.infrastructure.model.entity.PeriodEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

internal class CheckPeriodStrategyTest {

    companion object {
        private const val FORMAT_PATTERN = "uuuu-MM-dd'T'HH:mm:ss'Z'"

        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_PATTERN)
            .withResolverStyle(ResolverStyle.STRICT)
        private val ENTITY_START_DATE = LocalDateTime.parse("2020-02-10T08:49:55Z", FORMATTER)
        private val ENTITY_END_DATE = ENTITY_START_DATE.plusDays(10)
        private val REQUEST_START_DATE = LocalDateTime.parse("2020-02-12T08:49:55Z", FORMATTER)
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
    }

    @Nested
    inner class Execute {
        private val periodRepository: PeriodRepository = mock()
        private val strategy: CheckPeriodStrategy = CheckPeriodStrategy(periodRepository)

        @Test
        fun requestEndDateIsAfterStoredEndDate_success() {
            val periodEntity = stubPeriodEntity()
            whenever(periodRepository.findBy(cpid = CPID, ocid = OCID))
                .thenReturn(periodEntity.asSuccess())

            val startDateFormatted = REQUEST_START_DATE.format(FORMATTER)
            val endDate = periodEntity.endDate.plusDays(1)
            val endDateFormatted = endDate.format(FORMATTER)
            val data = createCheckPeriodData(startDate = startDateFormatted, endDate = endDateFormatted)

            val actual = strategy.execute(data = data, context = stubContext()).get

            val expected = CheckPeriodResult(
                isPreQualificationPeriodChanged = true,
                preQualification = CheckPeriodResult.PreQualification(
                    period = CheckPeriodResult.PreQualification.Period(
                        startDate = periodEntity.startDate, endDate = endDate
                    )
                )
            )

            assertEquals(expected, actual)
        }

        @Test
        fun requestEndDateEqualsStoredEndDate_success() {
            val periodEntity = stubPeriodEntity()
            whenever(periodRepository.findBy(cpid = CPID, ocid = OCID))
                .thenReturn(periodEntity.asSuccess())

            val startDate = REQUEST_START_DATE.format(FORMATTER)
            val endDate = periodEntity.endDate.format(FORMATTER)
            val data = createCheckPeriodData(startDate = startDate, endDate = endDate)

            val actual = strategy.execute(data = data, context = stubContext()).get

            val expected = CheckPeriodResult(
                isPreQualificationPeriodChanged = false,
                preQualification = CheckPeriodResult.PreQualification(
                    period = CheckPeriodResult.PreQualification.Period(
                        startDate = periodEntity.startDate,
                        endDate = periodEntity.endDate
                    )
                )
            )

            assertEquals(expected, actual)
        }

        @Test
        fun requestEndDatePrecedesStartDate_fail() {
            val periodEntity = stubPeriodEntity()
            whenever(periodRepository.findBy(cpid = CPID, ocid = OCID))
                .thenReturn(periodEntity.asSuccess())

            val startDate = REQUEST_START_DATE.format(FORMATTER)
            val endDate = REQUEST_START_DATE.minusDays(2).format(FORMATTER)
            val data = createCheckPeriodData(startDate = startDate, endDate = endDate)

            val actual = strategy.execute(data = data, context = stubContext()).error

            assertTrue(actual is ValidationError.CommandError.InvalidPeriodOnCheckPeriod)
        }

        @Test
        fun requestEndDatePrecedesStoredEndDate_fail() {
            val periodEntity = stubPeriodEntity()
            whenever(periodRepository.findBy(cpid = CPID, ocid = OCID))
                .thenReturn(periodEntity.asSuccess())

            val startDate = REQUEST_START_DATE.format(FORMATTER)
            val endDate = periodEntity.endDate.minusSeconds(1).format(FORMATTER)
            val data = createCheckPeriodData(startDate = startDate, endDate = endDate)

            val actual = strategy.execute(data = data, context = stubContext()).error

            assertTrue(actual is ValidationError.CommandError.InvalidPeriodEndDate)
        }

        private fun createCheckPeriodData(startDate: String, endDate: String): CheckPeriodData {
            val period = CheckPeriodData.Period.tryCreate(
                startDate = startDate, endDate = endDate
            ).get
            return CheckPeriodData.tryCreate(period).get
        }

        private fun stubPeriodEntity() =
            PeriodEntity(
                startDate = ENTITY_START_DATE,
                endDate = ENTITY_END_DATE,
                ocid = OCID,
                cpid = CPID
            )

        private fun stubContext() = CheckPeriodContext(cpid = CPID, ocid = OCID)
    }
}