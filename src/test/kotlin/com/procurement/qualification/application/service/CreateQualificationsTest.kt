package com.procurement.qualification.application.service

import com.procurement.qualification.application.model.params.CreateQualificationsParams
import com.procurement.qualification.domain.enums.ConversionRelatesTo
import com.procurement.qualification.domain.enums.QualificationSystemMethod
import com.procurement.qualification.domain.enums.ReductionCriteria
import com.procurement.qualification.domain.enums.RequirementDataType
import com.procurement.qualification.domain.enums.RequirementStatus
import com.procurement.qualification.domain.model.measure.Scoring
import com.procurement.qualification.domain.model.requirement.RequirementResponseValue
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientRate
import com.procurement.qualification.domain.model.tender.conversion.coefficient.CoefficientValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.util.*
import java.util.stream.Stream
import kotlin.streams.asStream

class CreateQualificationsTest {

    @ParameterizedTest
    @MethodSource
    @DisplayName("Matching coefficient value to requirement response value.")
    fun isMatchCoefficientValueAndRequirementValue(
        coefficientValue: CoefficientValue,
        requirementValue: RequirementResponseValue,
        result: Boolean
    ) {
        val actual = QualificationServiceImpl.isMatchCoefficientValueAndRequirementValue(coefficientValue, requirementValue)
        assertEquals(result, actual)
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("FR.COM-7.11.6 is need calculate scoring")
    fun isNeedCalculateScoring(reductionCriteria: ReductionCriteria, qualificationSystemMethod: QualificationSystemMethod, result: Boolean) {
        val actual = QualificationServiceImpl.isCalculateScoringNeeded(reductionCriteria, qualificationSystemMethod)
        assertEquals(result, actual)
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Getting coefficients for calculating scoring")
    fun getCoefficients(
        conversions: List<CreateQualificationsParams.Tender.Conversion>,
        requirementResponses: List<CreateQualificationsParams.Submission.RequirementResponse>,
        criteria: List<CreateQualificationsParams.Tender.Criterion>,
        result: List<CoefficientRate>
    ) {
        val actual = QualificationServiceImpl.getCoefficients(conversions, criteria, requirementResponses)
        assertEquals(result, actual)
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("FR.COM-7.11.7 calculate scoring with requirementResponse")
    fun calculateScoringWithRequirementResponse(coefficients: List<CoefficientRate>, result: Scoring) {
        val actual = QualificationServiceImpl.calculateScoring(coefficients)
        assertEquals(result, actual)
    }

    @Test
    @DisplayName("FR.COM-7.11.10 calculate scoring without requirementResponse")
    fun calculateScoringWithoutRequirementResponse() {
        val expected = Scoring(BigDecimal.ONE)
        val actual = QualificationServiceImpl.calculateScoring(coefficients = emptyList())
        assertEquals(expected, actual)
    }

    companion object {

        @JvmStatic
        fun isMatchCoefficientValueAndRequirementValue(): Stream<Arguments> = listOf(
            Arguments.of(
                CoefficientValue.AsBoolean(true),
                RequirementResponseValue.AsBoolean(true),
                true
            ),
            Arguments.of(
                CoefficientValue.AsBoolean(true),
                RequirementResponseValue.AsBoolean(false),
                false
            ),
            Arguments.of(
                CoefficientValue.AsBoolean(true),
                RequirementResponseValue.AsString("N/A"),
                false
            ),
            Arguments.of(
                CoefficientValue.AsString("Some"),
                RequirementResponseValue.AsString("Some"),
                true
            ),
            Arguments.of(
                CoefficientValue.AsString("Some"),
                RequirementResponseValue.AsString("Other"),
                false
            ),
            Arguments.of(
                CoefficientValue.AsString("Some"),
                RequirementResponseValue.AsBoolean(true),
                false
            ),
            Arguments.of(
                CoefficientValue.AsNumber(10.5.toBigDecimal()),
                RequirementResponseValue.AsNumber(10.5.toBigDecimal()),
                true
            ),
            Arguments.of(
                CoefficientValue.AsNumber(10.5.toBigDecimal()),
                RequirementResponseValue.AsNumber(10.0.toBigDecimal()),
                false
            ),
            Arguments.of(
                CoefficientValue.AsNumber(10.5.toBigDecimal()),
                RequirementResponseValue.AsString("N/A"),
                false
            ),
            Arguments.of(
                CoefficientValue.AsInteger(10),
                RequirementResponseValue.AsInteger(10),
                true
            ),
            Arguments.of(
                CoefficientValue.AsInteger(10),
                RequirementResponseValue.AsInteger(99),
                false
            ),
            Arguments.of(
                CoefficientValue.AsInteger(10),
                RequirementResponseValue.AsString("N/A"),
                false
            )
        ).asSequence().asStream()

        @JvmStatic
        fun isNeedCalculateScoring(): Stream<Arguments> {
            val list = mutableListOf<Arguments>()
            for (reductionCriteria in enumValues<ReductionCriteria>()) {
                for (qualificationSystemMethod in enumValues<QualificationSystemMethod>()) {
                    val result = when (reductionCriteria) {
                        ReductionCriteria.SCORING -> when (qualificationSystemMethod) {
                            QualificationSystemMethod.AUTOMATED -> true
                            QualificationSystemMethod.MANUAL -> false
                        }
                        ReductionCriteria.NONE -> when (qualificationSystemMethod) {
                            QualificationSystemMethod.AUTOMATED -> false
                            QualificationSystemMethod.MANUAL -> false
                        }
                    }
                    list.add(Arguments.of(reductionCriteria, qualificationSystemMethod, result))
                }
            }
            return list.asSequence().asStream()
        }

        @JvmStatic
        fun calculateScoringWithRequirementResponse(): Stream<Arguments> = listOf(
            Arguments.of(
                listOf(
                    CoefficientRate(1.toBigDecimal()),
                    CoefficientRate(0.33.toBigDecimal()),
                    CoefficientRate(0.25.toBigDecimal()),
                    CoefficientRate(0.15.toBigDecimal())
                ),
                Scoring(0.012.toBigDecimal())
            ),
            Arguments.of(
                listOf(
                    CoefficientRate(1.toBigDecimal()),
                    CoefficientRate(0.5.toBigDecimal()),
                    CoefficientRate(0.25.toBigDecimal()),
                    CoefficientRate(0.15.toBigDecimal())
                ),
                Scoring(0.019.toBigDecimal())
            )
        ).asSequence().asStream()


        @JvmStatic
        fun getCoefficients(): Stream<Arguments> = listOf(
            Arguments.of(
                emptyList<CreateQualificationsParams.Tender.Conversion>(),
                emptyList<CreateQualificationsParams.Submission.RequirementResponse>(),
                emptyList<CreateQualificationsParams.Tender.Criterion>(),
                result()
            ),
            Arguments.of(
                listOf(
                    conversion(
                        relatedItem = "requirement-id-1",
                        coefficients = listOf(
                            coefficient(value = 10, coefficient = 0.5),
                            coefficient(value = 20, coefficient = 0.25)
                        )
                    )
                ),
                emptyList<CreateQualificationsParams.Submission.RequirementResponse>(),
                listOf(criteria("requirement-id-1")),
                result()
            ),
            Arguments.of(
                listOf(
                    conversion(
                        relatedItem = "requirement-id-1",
                        coefficients = listOf(
                            coefficient(value = 10, coefficient = 0.5),
                            coefficient(value = 20, coefficient = 0.25)
                        )
                    )
                ),
                listOf(
                    requirementResponse(requirement = "requirement-id-1", value = 99)
                ),
                listOf(criteria("requirement-id-1")),
                result()
            ),
            Arguments.of(
                listOf(
                    conversion(
                        relatedItem = "requirement-id-1",
                        coefficients = listOf(
                            coefficient(value = 10, coefficient = 0.5),
                            coefficient(value = 20, coefficient = 0.25)
                        )
                    )
                ),
                listOf(
                    requirementResponse(requirement = "requirement-id-2", value = 10)
                ),
                listOf(criteria("requirement-id-1")),
                result()
            ),
            Arguments.of(
                listOf(
                    conversion(
                        relatedItem = "requirement-id-1",
                        coefficients = listOf(
                            coefficient(value = 10, coefficient = 0.5),
                            coefficient(value = 20, coefficient = 0.25)
                        )
                    ),
                    conversion(relatedItem = "requirement-id-2",
                        coefficients = listOf(
                            coefficient(value = 30, coefficient = 0.55),
                            coefficient(value = 40, coefficient = 0.33)
                        )
                    )
                ),
                listOf(
                    requirementResponse(requirement = "requirement-id-1", value = 10)
                ),
                listOf(criteria("requirement-id-1")),
                result(0.5)
            ),
            Arguments.of(
                listOf(
                    conversion(
                        relatedItem = "requirement-id-1",
                        coefficients = listOf(
                            coefficient(value = 10, coefficient = 0.5),
                            coefficient(value = 20, coefficient = 0.25)
                        )
                    ),
                    conversion(
                        relatedItem = "requirement-id-2",
                        coefficients = listOf(
                            coefficient(value = 30, coefficient = 0.55),
                            coefficient(value = 40, coefficient = 0.33)
                        )
                    )
                ),
                listOf(
                    requirementResponse(requirement = "requirement-id-1", value = 10),
                    requirementResponse(requirement = "requirement-id-2", value = 40)
                ),
                listOf(criteria("requirement-id-1"), criteria("requirement-id-2")),
                result(0.5, 0.33)
            )
        ).asSequence().asStream()

        private fun conversion(
            relatedItem: String,
            coefficients: List<CreateQualificationsParams.Tender.Conversion.Coefficient>
        ): CreateQualificationsParams.Tender.Conversion = CreateQualificationsParams.Tender.Conversion.tryCreate(
            id = UUID.randomUUID().toString(),
            relatedItem = relatedItem,
            relatesTo = ConversionRelatesTo.REQUIREMENT.key,
            rationale = "None",
            description = "Any",
            coefficients = coefficients
        ).get

        private fun coefficient(value: Long, coefficient: Double): CreateQualificationsParams.Tender.Conversion.Coefficient =
            CreateQualificationsParams.Tender.Conversion.Coefficient.tryCreate(
                id = UUID.randomUUID().toString(),
                value = CoefficientValue.AsInteger(value),
                coefficient = coefficientRate(coefficient)
            ).get

        private fun requirementResponse(requirement: String, value: Long): CreateQualificationsParams.Submission.RequirementResponse =
            CreateQualificationsParams.Submission.RequirementResponse.tryCreate(
                id = UUID.randomUUID().toString(),
                value = RequirementResponseValue.AsInteger(value),
                requirement = CreateQualificationsParams.Submission.RequirementResponse.Requirement.tryCreate(requirement).get,
                relatedCandidate = CreateQualificationsParams.Submission.RequirementResponse.RelatedCandidate.tryCreate(
                    id = "candidate-id-1",
                    name = "candidate-name-1"
                ).get
            ).get

        private fun criteria(requirement: String): CreateQualificationsParams.Tender.Criterion =
            CreateQualificationsParams.Tender.Criterion.tryCreate(
                id = UUID.randomUUID().toString(),
                relatesTo = "tenderer",
                source = "",
                classification = CreateQualificationsParams.Tender.Criterion.Classification(
                    id = "CRITERION.SELECTION.123",
                    scheme = ""
                ),
                requirementGroups = listOf(
                    CreateQualificationsParams.Tender.Criterion.RequirementGroup.tryCreate(
                        id = UUID.randomUUID().toString(),
                        requirements = listOf(
                            CreateQualificationsParams.Tender.Criterion.RequirementGroup.Requirement.tryCreate(
                                id = requirement,
                                status = RequirementStatus.ACTIVE.key,
                                dataType = RequirementDataType.INTEGER.key
                            ).get
                        )
                    ).get
                )
            ).get

        private fun result(vararg values: Double): List<CoefficientRate> = values.map { value -> coefficientRate(value) }

        private fun coefficientRate(value: Double) = CoefficientRate(value.toBigDecimal())
    }
}
