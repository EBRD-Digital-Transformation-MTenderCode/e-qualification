package com.procurement.qualification.application.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.qualification.application.model.params.AnalyzeQualificationsForInvitationParams
import com.procurement.qualification.application.model.params.DoConsiderationParams
import com.procurement.qualification.application.repository.QualificationRepository
import com.procurement.qualification.domain.enums.Pmd
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.functional.ValidationResult
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.measure.Scoring
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.submission.SubmissionId
import com.procurement.qualification.infrastructure.bind.databinding.JsonDateTimeDeserializer
import com.procurement.qualification.infrastructure.bind.databinding.JsonDateTimeSerializer
import com.procurement.qualification.infrastructure.handler.analyze.qualification.AnalyzeQualificationsForInvitationResult
import com.procurement.qualification.infrastructure.handler.check.qualification.protocol.CheckQualificationsForProtocolParams
import com.procurement.qualification.infrastructure.handler.create.consideration.DoConsiderationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class QualificationServiceImplTest {
    companion object {
        val CPID = Cpid.tryCreateOrNull("ocds-b3wdp1-MD-1580458690892") ?: throw RuntimeException()
        val OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1580458690892-EV-1580458791896") ?: throw RuntimeException()
        val COUNTRY = "MD"
        val PMD = Pmd.GPA
        private val DATE = JsonDateTimeDeserializer.deserialize(JsonDateTimeSerializer.serialize(LocalDateTime.now()))
        val QUALIFICATION_ID_FIRST = QualificationId.generate()
        val QUALIFICATION_ID_SECOND = QualificationId.generate()
    }

    val qualificationRepository: QualificationRepository = mock()
    val generationService: GenerationServiceImpl = mock()
    val rulesService: RulesServiceImpl = mock()
    val qualificationService = QualificationServiceImpl(qualificationRepository, generationService, rulesService)

    @Nested
    inner class DoConsideration {

        @Test
        fun success() {
            val params = getParams()
            val qualificationFirst = createQualification(id = QUALIFICATION_ID_FIRST)
            val qualificationSecond = createQualification(id = QUALIFICATION_ID_SECOND)

            whenever(
                qualificationRepository.findBy(
                    cpid = params.cpid,
                    ocid = params.ocid,
                    qualificationIds = params.qualifications.map { it.id })
            ).thenReturn(listOf(qualificationFirst, qualificationSecond).asSuccess())

            val expectedStatusDetails = QualificationStatusDetails.CONSIDERATION
            val firstQualification = DoConsiderationResult.Qualification(
                id = qualificationFirst.id, statusDetails = expectedStatusDetails
            )
            val secondQualification = DoConsiderationResult.Qualification(
                id = qualificationSecond.id, statusDetails = expectedStatusDetails
            )
            val expected = DoConsiderationResult(listOf(firstQualification, secondQualification))

            val actual = qualificationService.doConsideration(params = params).get

            assertEquals(expected, actual)
        }

        @Test
        fun verifyUpdateWasCalled() {
            val params = getParams()
            val qualificationFirst = createQualification(id = QUALIFICATION_ID_FIRST)
            val qualificationSecond = createQualification(id = QUALIFICATION_ID_SECOND)

            whenever(
                qualificationRepository.findBy(
                    cpid = params.cpid,
                    ocid = params.ocid,
                    qualificationIds = params.qualifications.map { it.id })
            ).thenReturn(listOf(qualificationFirst, qualificationSecond).asSuccess())

            val expectedStatusDetails = QualificationStatusDetails.CONSIDERATION
            val updatedQualificationFirst = qualificationFirst.copy(statusDetails = expectedStatusDetails)
            val updatedQualificationSecond = qualificationSecond.copy(statusDetails = expectedStatusDetails)

            val updatedQualifications = listOf(updatedQualificationFirst, updatedQualificationSecond)

            qualificationService.doConsideration(params = params)

            verify(qualificationRepository, times(1)).updateAll(
                cpid = params.cpid,
                ocid = params.ocid,
                qualifications = updatedQualifications
            )
        }

        @Test
        fun paramsContainUnknownQualifications_fail() {
            val params = getParams()
            val qualification = createQualification(id = QUALIFICATION_ID_FIRST)

            whenever(
                qualificationRepository.findBy(
                    cpid = params.cpid,
                    ocid = params.ocid,
                    qualificationIds = params.qualifications.map { it.id })
            ).thenReturn(listOf(qualification).asSuccess())

            val actual = qualificationService.doConsideration(params = params).error
            val expectedErrorCode = "VR.COM-7.21.1"

            assertEquals(expectedErrorCode, actual.code)
        }

        private fun getParams(): DoConsiderationParams {
            return DoConsiderationParams.tryCreate(
                cpid = CPID.toString(),
                ocid = OCID.toString(),
                qualifications = listOf(
                    DoConsiderationParams.Qualification.tryCreate(id = QUALIFICATION_ID_FIRST.toString()).get,
                    DoConsiderationParams.Qualification.tryCreate(id = QUALIFICATION_ID_SECOND.toString()).get

                )
            ).get
        }

        private fun createQualification(id: QualificationId) = Qualification(
            id = id,
            date = DATE,
            owner = UUID.randomUUID(),
            token = UUID.randomUUID(),
            status = QualificationStatus.ACTIVE,
            scoring = Scoring.tryCreate("0.001").get,
            statusDetails = QualificationStatusDetails.ACTIVE,
            relatedSubmission = SubmissionId.generate()
        )
    }

    @Nested
    inner class CheckQualificationsForProtocol {

        @Test
        fun success() {
            val params: CheckQualificationsForProtocolParams = getParams()
            val allowedStatus = QualificationStatus.PENDING
            val allowedStatusDetailsUnsuccessful = QualificationStatusDetails.UNSUCCESSFUL
            val allowedStatusDetailsActive = QualificationStatusDetails.ACTIVE

            val qualificationsStored = listOf(
                createQualification(allowedStatus, allowedStatusDetailsActive),
                createQualification(allowedStatus, allowedStatusDetailsUnsuccessful)
            )
            whenever(qualificationRepository.findBy(cpid = CPID, ocid = OCID))
                .thenReturn(qualificationsStored.asSuccess())
            val actual = qualificationService.checkQualificationsForProtocol(params = params)

            assertTrue(actual is ValidationResult.Ok)
        }

        @Test
        fun wrongStatus_fail() {
            val params: CheckQualificationsForProtocolParams = getParams()
            val wrongStatus = QualificationStatus.UNSUCCESSFUL
            val allowedStatus = QualificationStatus.PENDING
            val allowedStatusDetails = QualificationStatusDetails.ACTIVE

            val qualificationsStored = listOf(
                createQualification(allowedStatus, allowedStatusDetails),
                createQualification(wrongStatus, allowedStatusDetails)
            )
            whenever(qualificationRepository.findBy(cpid = CPID, ocid = OCID))
                .thenReturn(qualificationsStored.asSuccess())

            val actual = qualificationService.checkQualificationsForProtocol(params = params).error
            val expectedErrorCode = "VR.COM-7.24.2"
            val expectedErrorDescription = "Unsuitable qualification found by cpid '$CPID', ocid '$OCID', id '${qualificationsStored[1].id}'."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorDescription, actual.description)
        }

        @Test
        fun wrongStatusDetails_fail() {
            val params: CheckQualificationsForProtocolParams = getParams()
            val allowedStatus = QualificationStatus.PENDING
            val allowedStatusDetails = QualificationStatusDetails.ACTIVE
            val wrongStatusDetails = QualificationStatusDetails.CONSIDERATION

            val qualificationsStored = listOf(
                createQualification(allowedStatus, wrongStatusDetails),
                createQualification(allowedStatus, allowedStatusDetails)
            )
            whenever(qualificationRepository.findBy(cpid = CPID, ocid = OCID))
                .thenReturn(qualificationsStored.asSuccess())

            val actual = qualificationService.checkQualificationsForProtocol(params = params).error
            val expectedErrorCode = "VR.COM-7.24.2"
            val expectedErrorDescription = "Unsuitable qualification found by cpid '$CPID', ocid '$OCID', id '${qualificationsStored.first().id}'."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorDescription, actual.description)
        }

        @Test
        fun noQualificationFound_fail() {
            val params: CheckQualificationsForProtocolParams = getParams()

            whenever(qualificationRepository.findBy(cpid = CPID, ocid = OCID))
                .thenReturn(emptyList<Qualification>().asSuccess())

            val actual = qualificationService.checkQualificationsForProtocol(params = params).error
            val expectedErrorCode = "VR.COM-7.24.1"
            val expectedErrorDescription = "No qualification found by cpid='$CPID' and ocid='$OCID'."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorDescription, actual.description)
        }

        private fun getParams() = CheckQualificationsForProtocolParams.tryCreate(
            cpid = CPID.toString(),
            ocid = OCID.toString()
        ).get
    }

    @Nested
    inner class AnalyzeQualificationsForInvitation {

        @Test
        fun equalsMinimumQuantity_success() {
            val params: AnalyzeQualificationsForInvitationParams = getParams()
            val allowedStatus = QualificationStatus.PENDING
            val allowedStatusDetails = QualificationStatusDetails.ACTIVE

            val qualificationsStored = listOf(
                createQualification(allowedStatus, allowedStatusDetails),
                createQualification(allowedStatus, allowedStatusDetails)
            )
            whenever(qualificationRepository.findBy(cpid = CPID, ocid = OCID))
                .thenReturn(qualificationsStored.asSuccess())

            val minimumQuantity = 2L
            whenever(rulesService.findMinimumQualificationQuantity(country = COUNTRY, pmd = PMD))
                .thenReturn(minimumQuantity.asSuccess())

            val actual = qualificationService.analyzeQualificationsForInvitation(params).get
            val expected = AnalyzeQualificationsForInvitationResult(qualificationsStored.map {qualification ->
                AnalyzeQualificationsForInvitationResult.Qualification(
                    id = qualification.id,
                    status = qualification.status,
                    relatedSubmission = qualification.relatedSubmission,
                    statusDetails = qualification.statusDetails!!
                )
            })

            assertEquals(expected, actual)
        }

        @Test
        fun exceedsMinimumQuantity_success() {
            val params: AnalyzeQualificationsForInvitationParams = getParams()
            val allowedStatus = QualificationStatus.PENDING
            val allowedStatusDetails = QualificationStatusDetails.ACTIVE

            val qualificationsStored = listOf(
                createQualification(allowedStatus, allowedStatusDetails),
                createQualification(allowedStatus, allowedStatusDetails)
            )
            whenever(qualificationRepository.findBy(cpid = CPID, ocid = OCID))
                .thenReturn(qualificationsStored.asSuccess())

            val minimumQuantity = 1L
            whenever(rulesService.findMinimumQualificationQuantity(country = COUNTRY, pmd = PMD))
                .thenReturn(minimumQuantity.asSuccess())

            val actual = qualificationService.analyzeQualificationsForInvitation(params).get
            val expected = AnalyzeQualificationsForInvitationResult(qualificationsStored.map {qualification ->
                AnalyzeQualificationsForInvitationResult.Qualification(
                    id = qualification.id,
                    status = qualification.status,
                    relatedSubmission = qualification.relatedSubmission,
                    statusDetails = qualification.statusDetails!!
                )
            })

            assertEquals(expected, actual)
        }

        @Test
        fun wrongStatusMakesQuantityLessThanMinimum_nullResult() {
            val params: AnalyzeQualificationsForInvitationParams = getParams()
            val wrongStatus = QualificationStatus.UNSUCCESSFUL
            val allowedStatusDetails = QualificationStatusDetails.ACTIVE

            val qualificationsStored = listOf(
                createQualification(wrongStatus, allowedStatusDetails),
                createQualification(wrongStatus, allowedStatusDetails)
            )
            whenever(qualificationRepository.findBy(cpid = CPID, ocid = OCID))
                .thenReturn(qualificationsStored.asSuccess())

            val minimumQuantity = 2L
            whenever(rulesService.findMinimumQualificationQuantity(country = COUNTRY, pmd = PMD))
                .thenReturn(minimumQuantity.asSuccess())

            val actual = qualificationService.analyzeQualificationsForInvitation(params).get

            assertTrue(actual == null)
        }

        @Test
        fun wrongStatusDetailsMakesQuantityLessThanMinimum_nullResult() {
            val params: AnalyzeQualificationsForInvitationParams = getParams()
            val allowedStatus = QualificationStatus.PENDING
            val wrongStatusDetails = QualificationStatusDetails.AWAITING

            val qualificationsStored = listOf(
                createQualification(allowedStatus, wrongStatusDetails),
                createQualification(allowedStatus, wrongStatusDetails)
            )
            whenever(qualificationRepository.findBy(cpid = CPID, ocid = OCID))
                .thenReturn(qualificationsStored.asSuccess())

            val minimumQuantity = 2L
            whenever(rulesService.findMinimumQualificationQuantity(country = COUNTRY, pmd = PMD))
                .thenReturn(minimumQuantity.asSuccess())

            val actual = qualificationService.analyzeQualificationsForInvitation(params).get

            assertTrue(actual == null)
        }

        @Test
        fun quantityRuleNotFound_nullResult() {
            val params: AnalyzeQualificationsForInvitationParams = getParams()
            val allowedStatus = QualificationStatus.PENDING
            val wrongStatusDetails = QualificationStatusDetails.AWAITING

            val qualificationsStored = listOf(
                createQualification(allowedStatus, wrongStatusDetails),
                createQualification(allowedStatus, wrongStatusDetails)
            )
            whenever(qualificationRepository.findBy(cpid = CPID, ocid = OCID))
                .thenReturn(qualificationsStored.asSuccess())

            whenever(rulesService.findMinimumQualificationQuantity(country = COUNTRY, pmd = PMD))
                .thenReturn(null.asSuccess())

            val actual = qualificationService.analyzeQualificationsForInvitation(params).error
            val expectedErrorCode = "VR.COM-17"
            val expectedErrorDescription = "Rule not found by country '$COUNTRY', pmd '$PMD'."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorDescription, actual.description)
        }

        private fun getParams() = AnalyzeQualificationsForInvitationParams.tryCreate(
            cpid = CPID.toString(), ocid = OCID.toString(), country = COUNTRY, pmd = PMD.toString()
        ).get

    }

    private fun createQualification(
        status: QualificationStatus,
        statusDetails: QualificationStatusDetails
    ) = Qualification(
        id = QualificationId.generate(),
        date = DATE,
        owner = UUID.randomUUID(),
        token = UUID.randomUUID(),
        status = status,
        scoring = Scoring.tryCreate("0.001").get,
        statusDetails = statusDetails,
        relatedSubmission = SubmissionId.generate()
    )
}