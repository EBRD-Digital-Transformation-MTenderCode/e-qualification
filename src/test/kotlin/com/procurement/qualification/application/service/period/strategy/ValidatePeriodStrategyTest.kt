package com.procurement.qualification.application.service.period.strategy

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.qualification.application.repository.PeriodRulesRepository
import com.procurement.qualification.domain.enums.ProcurementMethod
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.data.period.ValidatePeriodContext
import com.procurement.qualification.domain.model.data.period.ValidatePeriodData
import com.procurement.qualification.infrastructure.fail.error.ValidationError
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

internal class ValidatePeriodStrategyTest {
    companion object {
        private const val COUNTRY = "MD"
        private val PMD = ProcurementMethod.GPA
        private val ALLOWED_TERM = TimeUnit.DAYS.toSeconds(10)
    }
    @Nested
    inner class Execute {

        private val periodRulesRepository: PeriodRulesRepository = mock()
        private val strategy: ValidatePeriodStrategy = ValidatePeriodStrategy(periodRulesRepository)

        @Test
        fun periodDurationEqualsAllowedTerm_success() {
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(ALLOWED_TERM.asSuccess())
            val data = createValidatePeriodData(startDate = "2020-02-10T08:49:55Z", endDate = "2020-02-20T08:49:55Z")
            val context = stubContext()

            val actual = strategy.execute(data = data, context = context)

            assertTrue(actual.isOk)
        }

        @Test
        fun periodDurationGreaterThanAllowedTerm_success() {
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(ALLOWED_TERM.asSuccess())
            val data = createValidatePeriodData(startDate = "2020-02-10T08:49:55Z", endDate = "2020-02-22T08:49:55Z")
            val context = stubContext()

            val actual = strategy.execute(data = data, context = context)

            assertTrue(actual.isOk)
        }

        @Test
        fun startAndEndDateEqual_fail(){
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(ALLOWED_TERM.asSuccess())
            val date = "2020-02-10T08:49:55Z"
            val data = createValidatePeriodData(startDate = date, endDate = date)
            val context = stubContext()

            val actual = strategy.execute(data = data, context = context)

            assertTrue(actual.error is ValidationError.CommandError.InvalidPeriod)
        }

        @Test
        fun endDatePrecedesStartDate_fail(){
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(ALLOWED_TERM.asSuccess())
            val data = createValidatePeriodData(startDate = "2020-02-10T08:49:55Z", endDate = "2020-02-09T08:49:55Z")
            val context = stubContext()

            val actual = strategy.execute(data = data, context = context)

            assertTrue(actual.error is ValidationError.CommandError.InvalidPeriod)
        }

        @Test
        fun periodDurationLessThanTenDaysByOneSecond_fail(){
            whenever(periodRulesRepository.findTermBy(pmd = PMD, country = COUNTRY))
                .thenReturn(ALLOWED_TERM.asSuccess())
            val data = createValidatePeriodData(startDate = "2020-02-10T08:49:55Z", endDate = "2020-02-20T08:49:54Z")
            val context = stubContext()

            val actual = strategy.execute(data = data, context = context)

            assertTrue(actual.error is ValidationError.CommandError.InvalidPeriodTerm)
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