package com.procurement.qualification.application.service.period

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.qualification.application.model.period.save.SavePeriodContext
import com.procurement.qualification.application.model.period.save.SavePeriodData
import com.procurement.qualification.application.model.period.save.SavePeriodResult
import com.procurement.qualification.application.model.period.validate.ValidatePeriodContext
import com.procurement.qualification.application.model.period.validate.ValidatePeriodData
import com.procurement.qualification.application.repository.PeriodRepository
import com.procurement.qualification.application.repository.PeriodRulesRepository
import com.procurement.qualification.domain.enums.ProcurementMethod
import com.procurement.qualification.domain.enums.Stage
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.fail.error.ValidationError
import com.procurement.qualification.infrastructure.model.entity.PeriodEntity
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.concurrent.TimeUnit

internal class PeriodServiceTest {

    companion object {
        private const val FORMAT_PATTERN = "uuuu-MM-dd'T'HH:mm:ss'Z'"
        private const val COUNTRY = "MD"

        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_PATTERN)
            .withResolverStyle(ResolverStyle.STRICT)
        private val DATE = LocalDateTime.parse("2020-02-10T08:49:55Z", FORMATTER)
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
        private val STAGE = Stage.AC
        private val PMD = ProcurementMethod.GPA
        private val ALLOWED_TERM = TimeUnit.DAYS.toSeconds(10)
    }

    private val periodRepository: PeriodRepository = mock()
    private val periodRulesRepository: PeriodRulesRepository = mock()
    private val periodService = PeriodService(periodRepository, periodRulesRepository)

    @Nested
    inner class ValidatePeriod {

        @Test
        fun periodDurationEqualsAllowedTerm_success() {
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(ALLOWED_TERM.asSuccess())

            val endDate = DATE.plusDays(10).format(FORMATTER)
            val startDate = DATE.format(FORMATTER)
            val data = createValidatePeriodData(startDate = startDate, endDate = endDate)
            val context = stubContext()

            val actual = periodService.validatePeriod(data = data, context = context)

            assertTrue(actual.isOk)
        }

        @Test
        fun periodDurationGreaterThanAllowedTerm_success() {
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(ALLOWED_TERM.asSuccess())

            val endDate = DATE.plusDays(10).plusSeconds(1).format(FORMATTER)
            val startDate = DATE.format(FORMATTER)
            val data = createValidatePeriodData(startDate = startDate, endDate = endDate)
            val context = stubContext()

            val actual = periodService.validatePeriod(data = data, context = context)

            assertTrue(actual.isOk)
        }

        @Test
        fun startAndEndDateEqual_fail() {
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(ALLOWED_TERM.asSuccess())

            val date = DATE.format(FORMATTER)
            val data = createValidatePeriodData(startDate = date, endDate = date)
            val context = stubContext()

            val actual = periodService.validatePeriod(data = data, context = context)

            assertTrue(actual.error is ValidationError.CommandError.InvalidPeriod)
        }

        @Test
        fun endDatePrecedesStartDate_fail() {
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(ALLOWED_TERM.asSuccess())

            val startDate = DATE.plusDays(10).plusSeconds(1).format(
                FORMATTER
            )
            val endDate = DATE.format(FORMATTER)

            val data = createValidatePeriodData(startDate = startDate, endDate = endDate)
            val context = stubContext()

            val actual = periodService.validatePeriod(data = data, context = context)

            assertTrue(actual.error is ValidationError.CommandError.InvalidPeriod)
        }

        @Test
        fun periodDurationLessThanTenDaysByOneSecond_fail() {
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(ALLOWED_TERM.asSuccess())

            val endDate = DATE.plusDays(10).minusSeconds(1).format(FORMATTER)
            val startDate = DATE.format(FORMATTER)
            val data = createValidatePeriodData(startDate = startDate, endDate = endDate)
            val context = stubContext()

            val actual = periodService.validatePeriod(data = data, context = context)

            assertTrue(actual.error is ValidationError.CommandError.InvalidPeriodTerm)
        }

        @Test
        fun periodDurationRuleNotFound_fail() {
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(null.asSuccess())

            val endDate = DATE.plusDays(10).format(FORMATTER)
            val startDate = DATE.format(FORMATTER)
            val data = createValidatePeriodData(startDate = startDate, endDate = endDate)
            val context = stubContext()

            val actual = periodService.validatePeriod(data = data, context = context)

            assertTrue(actual.error is ValidationError.CommandError.PeriodRuleNotFound)
        }

        private fun createValidatePeriodData(startDate: String, endDate: String): ValidatePeriodData {
            val period = ValidatePeriodData.Period.tryCreate(
                startDate = startDate, endDate = endDate
            ).get
            return ValidatePeriodData.tryCreate(period).get
        }

        private fun stubContext() = ValidatePeriodContext(pmd = PMD, country = COUNTRY)
    }

    @Nested
    inner class SavePeriod {
        @Test
        fun repositoryMethod_isCalled() {
            val endDate = DATE.plusDays(10).format(FORMATTER)
            val startDate = DATE.format(FORMATTER)
            val data = createSavePeriodData(startDate = startDate, endDate = endDate)
            val context = SavePeriodContext(cpid = CPID, ocid = OCID, stage = STAGE)

            whenever(periodRepository.saveOrUpdatePeriod(any())).thenReturn(ValidationResult.ok())
            periodService.savePeriod(data = data, context = context)

            verify(periodRepository).saveOrUpdatePeriod(
                period = PeriodEntity(
                    cpid = context.cpid,
                    ocid = context.ocid,
                    endDate = data.period.endDate,
                    startDate = data.period.startDate
                )
            )
        }

        @Test
        fun success() {
            val endDate = DATE.plusDays(10).format(FORMATTER)
            val startDate = DATE.format(FORMATTER)
            val data = createSavePeriodData(startDate = startDate, endDate = endDate)
            val context = SavePeriodContext(cpid = CPID, ocid = OCID, stage = STAGE)

            whenever(periodRepository.saveOrUpdatePeriod(any())).thenReturn(ValidationResult.ok())
            val actual = periodService.savePeriod(data = data, context = context).get

            assertTrue(actual == SavePeriodResult)
        }

        @Test
        fun repositoryReturnedError_fail() {
            val endDate = DATE.plusDays(10).format(FORMATTER)
            val startDate = DATE.format(FORMATTER)
            val data = createSavePeriodData(startDate = startDate, endDate = endDate)
            val context = SavePeriodContext(cpid = CPID, ocid = OCID, stage = STAGE)

            val error = Fail.Incident.Database.DatabaseInteractionIncident(exception = RuntimeException())

            whenever(periodRepository.saveOrUpdatePeriod(any())).thenReturn(ValidationResult.error(error))
            val actual = periodService.savePeriod(data = data, context = context)

            assertTrue(actual.error == error)
        }

        private fun createSavePeriodData(startDate: String, endDate: String): SavePeriodData {
            val period = SavePeriodData.Period.tryCreate(
                startDate = startDate, endDate = endDate
            ).get
            return SavePeriodData.tryCreate(period).get
        }
    }
}