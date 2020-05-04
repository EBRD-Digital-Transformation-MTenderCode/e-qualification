package com.procurement.qualification.application.service.period.strategy

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.qualification.application.model.period.validate.ValidatePeriodContext
import com.procurement.qualification.application.model.period.validate.ValidatePeriodData
import com.procurement.qualification.application.repository.PeriodRulesRepository
import com.procurement.qualification.domain.enums.ProcurementMethod
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.infrastructure.fail.error.ValidationError
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.concurrent.TimeUnit

internal class ValidatePeriodStrategyTest {
    companion object {
        private const val COUNTRY = "MD"
        private val PMD = ProcurementMethod.GPA
        private val ALLOWED_TERM = TimeUnit.DAYS.toSeconds(10)

        private const val FORMAT_PATTERN = "uuuu-MM-dd'T'HH:mm:ss'Z'"
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_PATTERN)
            .withResolverStyle(ResolverStyle.STRICT)
        private val DATE = LocalDateTime.parse("2020-02-10T08:49:55Z", FORMATTER)
    }

    @Nested
    inner class Execute {

        private val periodRulesRepository: PeriodRulesRepository = mock()
        private val strategy: ValidatePeriodStrategy = ValidatePeriodStrategy(periodRulesRepository)

        @Test
        fun periodDurationEqualsAllowedTerm_success() {
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(ALLOWED_TERM.asSuccess())

            val endDate = DATE.plusDays(10).format(FORMATTER)
            val startDate = DATE.format(FORMATTER)
            val data = createValidatePeriodData(startDate = startDate, endDate = endDate)
            val context = stubContext()

            val actual = strategy.execute(data = data, context = context)

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

            val actual = strategy.execute(data = data, context = context)

            assertTrue(actual.isOk)
        }

        @Test
        fun startAndEndDateEqual_fail() {
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(ALLOWED_TERM.asSuccess())

            val date = DATE.format(FORMATTER)
            val data = createValidatePeriodData(startDate = date, endDate = date)
            val context = stubContext()

            val actual = strategy.execute(data = data, context = context)

            assertTrue(actual.error is ValidationError.CommandError.InvalidPeriod)
        }

        @Test
        fun endDatePrecedesStartDate_fail() {
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(ALLOWED_TERM.asSuccess())

            val startDate = DATE.plusDays(10).plusSeconds(1).format(FORMATTER)
            val endDate = DATE.format(FORMATTER)

            val data = createValidatePeriodData(startDate = startDate, endDate = endDate)
            val context = stubContext()

            val actual = strategy.execute(data = data, context = context)

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

            val actual = strategy.execute(data = data, context = context)

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

            val actual = strategy.execute(data = data, context = context)

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
}