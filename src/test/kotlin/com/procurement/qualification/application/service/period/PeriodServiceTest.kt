package com.procurement.qualification.application.service.period

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.qualification.application.model.period.check.CheckPeriodContext
import com.procurement.qualification.application.model.period.check.CheckPeriodData
import com.procurement.qualification.application.model.period.check.CheckPeriodResult
import com.procurement.qualification.application.model.period.save.SavePeriodContext
import com.procurement.qualification.application.model.period.save.SavePeriodData
import com.procurement.qualification.application.model.period.save.SavePeriodResult
import com.procurement.qualification.application.model.period.validate.ValidatePeriodContext
import com.procurement.qualification.application.model.period.validate.ValidatePeriodData
import com.procurement.qualification.application.repository.PeriodRepository
import com.procurement.qualification.application.service.period.strategy.CheckPeriodStrategy
import com.procurement.qualification.application.service.period.strategy.ValidatePeriodStrategy
import com.procurement.qualification.domain.enums.ProcurementMethod
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.PeriodEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

internal class PeriodServiceTest {

    companion object {
        private const val FORMAT_PATTERN = "uuuu-MM-dd'T'HH:mm:ss'Z'"
        private const val COUNTRY = "MD"

        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_PATTERN)
            .withResolverStyle(ResolverStyle.STRICT)
        private val REQUEST_START_DATE = LocalDateTime.parse("2020-02-10T08:49:55Z", FORMATTER)
        private val REQUEST_END_DATE = REQUEST_START_DATE.plusDays(10)
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
        private val PMD = ProcurementMethod.GPA
    }

    private val periodRepository: PeriodRepository = mock()
    private val checkPeriodStrategy: CheckPeriodStrategy = mock()
    private val validatePeriodStrategy: ValidatePeriodStrategy = mock()
    private val periodService = PeriodService(periodRepository, validatePeriodStrategy, checkPeriodStrategy)

    @Nested
    inner class ValidatePeriod {

        @Test
        @DisplayName("Check that result of validatePeriodStrategy.execute() call is returned")
        fun success() {
            val expected = ValidationResult.ok<Fail>()

            val data = stubData()
            val context = stubContext()

            whenever(validatePeriodStrategy.execute(data, context)).thenReturn(expected)

            val actual = periodService.validatePeriod(data, context)

            assertTrue(expected === actual)
        }

        private fun stubData(): ValidatePeriodData {
            val startDate = LocalDateTime.now()
            val startDateFormatted = startDate.format(FORMATTER)
            val endDataFormatted = startDate.plusDays(3).format(FORMATTER)
            val period = ValidatePeriodData.Period.tryCreate(
                startDate = startDateFormatted, endDate = endDataFormatted
            ).get
            return ValidatePeriodData.tryCreate(period).get
        }

        private fun stubContext() = ValidatePeriodContext(country = COUNTRY, pmd = PMD)
    }

    @Nested
    inner class SavePeriod {
        @Test
        fun repositoryMethod_isCalled() {
            val startDate = REQUEST_START_DATE.format(FORMATTER)
            val endDate = REQUEST_END_DATE.format(FORMATTER)
            val data = createSavePeriodData(startDate = startDate, endDate = endDate)
            val context = SavePeriodContext(cpid = CPID, ocid = OCID)

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
            val startDate = REQUEST_START_DATE.format(FORMATTER)
            val endDate = REQUEST_END_DATE.format(FORMATTER)
            val data = createSavePeriodData(startDate = startDate, endDate = endDate)
            val context = SavePeriodContext(cpid = CPID, ocid = OCID)

            whenever(periodRepository.saveOrUpdatePeriod(any())).thenReturn(ValidationResult.ok())
            val actual = periodService.savePeriod(data = data, context = context).get

            assertTrue(actual == SavePeriodResult)
        }

        @Test
        fun repositoryReturnedError_fail() {
            val startDate = REQUEST_START_DATE.format(FORMATTER)
            val endDate = REQUEST_END_DATE.format(FORMATTER)
            val data = createSavePeriodData(startDate = startDate, endDate = endDate)
            val context = SavePeriodContext(cpid = CPID, ocid = OCID)

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

    @Nested
    inner class CheckPeriod {
        @Test
        @DisplayName("Check that result of checkPeriodStrategy.execute() call is returned")
        fun success() {
            val expected = CheckPeriodResult(
                preQualificationPeriodChanged = true,
                preQualification = CheckPeriodResult.PreQualification(
                    period = CheckPeriodResult.PreQualification.Period(
                        startDate = LocalDateTime.now(), endDate = LocalDateTime.now()
                    )
                )
            )

            val data = stubCheckPeriodData()
            val context = stubContext()

            whenever(checkPeriodStrategy.execute(data, context)).thenReturn(expected.asSuccess())

            val actual = periodService.checkPeriod(data, context).get

            assertTrue(expected === actual)
            assertEquals(expected, actual)
        }

        private fun stubCheckPeriodData(): CheckPeriodData {
            val startDate = REQUEST_START_DATE.format(FORMATTER)
            val endDate = REQUEST_END_DATE.format(FORMATTER)
            val period = CheckPeriodData.Period.tryCreate(
                startDate = startDate, endDate = endDate
            ).get
            return CheckPeriodData.tryCreate(period).get
        }

        private fun stubContext() = CheckPeriodContext(cpid = CPID, ocid = OCID)
    }
}