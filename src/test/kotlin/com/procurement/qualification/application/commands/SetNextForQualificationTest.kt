package com.procurement.qualification.application.commands

import com.procurement.qualification.application.service.getQualificationsForProcessing
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.model.Owner
import com.procurement.qualification.domain.model.Token
import com.procurement.qualification.domain.model.measure.Scoring
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.domain.model.submission.SubmissionId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class SetNextForQualificationTest {

    companion object {
        private val SAMPLE_TIME = LocalDateTime.now()
        private val SAMPLE_UUID = UUID.randomUUID()
    }

    @Test
    @DisplayName("FR.COM-7.22.1")
    fun `is need items for returning - consideration & awaiting at start`() {
        val qualifications = listOf(
            qualificationSample().copy(statusDetails = QualificationStatusDetails.AWAITING),
            qualificationSample().copy(statusDetails = QualificationStatusDetails.CONSIDERATION),
            qualificationSample().copy(statusDetails = QualificationStatusDetails.UNSUCCESSFUL)
        )

        val expectedResult = emptyList<Qualification>()
        val actualResult = getQualificationsForProcessing(qualifications)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    @DisplayName("FR.COM-7.22.1")
    fun `is need items for returning - consideration & awaiting in the end`() {
        val qualifications = listOf(
            qualificationSample().copy(statusDetails = QualificationStatusDetails.UNSUCCESSFUL),
            qualificationSample().copy(statusDetails = QualificationStatusDetails.AWAITING),
            qualificationSample().copy(statusDetails = QualificationStatusDetails.CONSIDERATION)
        )

        val expectedResult = emptyList<Qualification>()
        val actualResult = getQualificationsForProcessing(qualifications)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    @DisplayName("FR.COM-7.22.1")
    fun `is need items for returning - null - first, awaiting - last `() {
        val qualifications = listOf(
            qualificationSample().copy(statusDetails = null),
            qualificationSample().copy(statusDetails = QualificationStatusDetails.UNSUCCESSFUL),
            qualificationSample().copy(statusDetails = QualificationStatusDetails.AWAITING)
        )

        val expectedResult = emptyList<Qualification>()
        val actualResult = getQualificationsForProcessing(qualifications)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    @DisplayName("FR.COM-7.22.1")
    fun `is need items for returning - without nulls, without awaiting || consideration`() {
        val qualifications = listOf(
            qualificationSample().copy(statusDetails = QualificationStatusDetails.UNSUCCESSFUL),
            qualificationSample().copy(statusDetails = QualificationStatusDetails.ACTIVE)
        )

        val expectedResult = emptyList<Qualification>()
        val actualResult = getQualificationsForProcessing(qualifications)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    @DisplayName("FR.COM-7.22.1")
    fun `is need items for returning - null's without awaiting `() {
        val qualifications = listOf(
            qualificationSample().copy(statusDetails = QualificationStatusDetails.UNSUCCESSFUL),
            qualificationSample().copy(statusDetails = QualificationStatusDetails.ACTIVE),
            qualificationSample().copy(statusDetails = null)
        )

        val expectedResult = qualifications.filter { it.statusDetails == null }
        val actualResult = getQualificationsForProcessing(qualifications)

        assertEquals(expectedResult.size, actualResult.size)
        assertTrue(expectedResult.filter { it.statusDetails != null }.isEmpty())
    }

    private val qualificationSample: () -> Qualification = {
        val sampleUuid = SAMPLE_UUID.toString()
        Qualification(
            id = QualificationId.tryCreateOrNull(UUID.randomUUID().toString())!!,
            status = QualificationStatus.UNSUCCESSFUL,
            statusDetails = null,
            date = SAMPLE_TIME,
            token = Token.fromString(sampleUuid),
            relatedSubmission = SubmissionId.tryCreateOrNull(sampleUuid)!!,
            owner = Owner.fromString(sampleUuid),
            scoring = Scoring(BigDecimal.TEN)
        )
    }
}