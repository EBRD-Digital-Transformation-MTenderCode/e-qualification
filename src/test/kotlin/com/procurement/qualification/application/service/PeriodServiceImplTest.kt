package com.procurement.qualification.application.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.qualification.application.model.params.CheckQualificationPeriodParams
import com.procurement.qualification.application.repository.PeriodRepository
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.util.extension.format
import com.procurement.qualification.infrastructure.fail.error.ValidationError
import com.procurement.qualification.infrastructure.model.entity.PeriodEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

internal class PeriodServiceImplTest {
    companion object {
        val CPID = Cpid.tryCreateOrNull("ocds-b3wdp1-MD-1580458690892") ?: throw RuntimeException()
        val OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1580458690892-EV-1580458791896") ?: throw RuntimeException()

        private const val FORMAT_PATTERN = "uuuu-MM-dd'T'HH:mm:ss'Z'"
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_PATTERN)
            .withResolverStyle(ResolverStyle.STRICT)
        private val DATE = LocalDateTime.parse("2020-02-10T08:49:55Z", FORMATTER)
    }

    val periodRepository: PeriodRepository = mock()
    val periodService = PeriodServiceImpl(periodRepository)

    @Nested
    inner class CheckQualificationPeriod {
        @Test
        fun success() {
            val requestDate = DATE
            val params: CheckQualificationPeriodParams = getParams(requestDate)
            val periodEntity = stubPeriodEntity(
                startDate = requestDate.minusDays(1),
                endDate = requestDate.plusDays(1)
            )
            whenever(periodRepository.findBy(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(periodEntity.asSuccess())

            val actual = periodService.checkQualificationPeriod(params = params)

            assertTrue(actual is ValidationResult.Ok)
        }

        @Test
        fun requestDateIsOneSecondAfterStartDate_success() {
            val requestDate = DATE
            val params: CheckQualificationPeriodParams = getParams(requestDate)
            val periodEntity = stubPeriodEntity(
                startDate = requestDate.minusSeconds(1),
                endDate = requestDate.plusDays(1)
            )
            whenever(periodRepository.findBy(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(periodEntity.asSuccess())

            val actual = periodService.checkQualificationPeriod(params = params)

            assertTrue(actual is ValidationResult.Ok)
        }

        @Test
        fun requestDateIsOneSecondBeforeEndDateDate_success() {
            val requestDate = DATE
            val params: CheckQualificationPeriodParams = getParams(requestDate)
            val periodEntity = stubPeriodEntity(
                startDate = requestDate.minusDays(1),
                endDate = requestDate.plusSeconds(1)
            )
            whenever(periodRepository.findBy(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(periodEntity.asSuccess())

            val actual = periodService.checkQualificationPeriod(params = params)

            assertTrue(actual is ValidationResult.Ok)
        }

        @Test
        fun requestDateEqualsStartDate_fail() {
            val requestDate = DATE
            val params: CheckQualificationPeriodParams = getParams(requestDate)
            val periodEntity = stubPeriodEntity(
                startDate = requestDate,
                endDate = requestDate.plusDays(1)
            )
            whenever(periodRepository.findBy(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(periodEntity.asSuccess())

            val actual = periodService.checkQualificationPeriod(params = params).error

            assertTrue(actual is ValidationError.RequestDateIsNotAfterStartDate)
            assertEquals(actual.code, "VR.COM-7.4.3")
        }

        @Test
        fun requestDateIsBeforeStartDate_fail() {
            val requestDate = DATE
            val params: CheckQualificationPeriodParams = getParams(requestDate)
            val periodEntity = stubPeriodEntity(
                startDate = requestDate.plusSeconds(1),
                endDate = requestDate.plusDays(1)
            )
            whenever(periodRepository.findBy(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(periodEntity.asSuccess())

            val actual = periodService.checkQualificationPeriod(params = params).error

            assertTrue(actual is ValidationError.RequestDateIsNotAfterStartDate)
            assertEquals(actual.code, "VR.COM-7.4.3")
        }

        @Test
        fun requestDateEqualsEndDate_fail() {
            val requestDate = DATE
            val params: CheckQualificationPeriodParams = getParams(requestDate)
            val periodEntity = stubPeriodEntity(
                startDate = requestDate.minusDays(1),
                endDate = requestDate
            )
            whenever(periodRepository.findBy(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(periodEntity.asSuccess())

            val actual = periodService.checkQualificationPeriod(params = params).error

            assertTrue(actual is ValidationError.RequestDateIsNotBeforeEndDate)
            assertEquals(actual.code, "VR.COM-7.4.4")
        }

        @Test
        fun requestDateIsAfterEndDate_fail() {
            val requestDate = DATE
            val params: CheckQualificationPeriodParams = getParams(requestDate)
            val periodEntity = stubPeriodEntity(
                startDate = requestDate.minusDays(1),
                endDate = requestDate.minusSeconds(1)
            )
            whenever(periodRepository.findBy(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(periodEntity.asSuccess())

            val actual = periodService.checkQualificationPeriod(params = params).error

            assertTrue(actual is ValidationError.RequestDateIsNotBeforeEndDate)
            assertEquals(actual.code, "VR.COM-7.4.4")
        }

        private fun getParams(date: LocalDateTime) = CheckQualificationPeriodParams.tryCreate(
            cpid = CPID.toString(),
            ocid = OCID.toString(),
            date = DATE.format()
        ).get

        private fun stubPeriodEntity(startDate: LocalDateTime, endDate: LocalDateTime) =
            PeriodEntity(
                startDate = startDate,
                endDate = endDate,
                ocid = OCID,
                cpid = CPID
            )
    }
}