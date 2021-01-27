package com.procurement.qualification.application.model.params

import com.procurement.qualification.application.model.parseCpid
import com.procurement.qualification.application.model.parseDate
import com.procurement.qualification.application.model.parseEnum
import com.procurement.qualification.application.model.parseOcid
import com.procurement.qualification.application.model.parseQualificationId
import com.procurement.qualification.domain.enums.DocumentType
import com.procurement.qualification.domain.enums.QualificationStatusDetails
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.asFailure
import com.procurement.qualification.domain.functional.asSuccess
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.document.DocumentId
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.infrastructure.fail.error.DataErrors
import java.time.LocalDateTime

class DoQualificationParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val date: LocalDateTime,
    val qualifications: List<Qualification>
) {

    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            date: String,
            qualifications: List<Qualification>
        ): Result<DoQualificationParams, DataErrors> {

            val parsedCpid = parseCpid(value = cpid)
                .orForwardFail { fail -> return fail }

            val parsedOcid = parseOcid(value = ocid)
                .orForwardFail { fail -> return fail }

            val parsedDate = parseDate(date, "date")
                .orForwardFail { fail -> return fail }

            return DoQualificationParams(
                cpid = parsedCpid,
                ocid = parsedOcid,
                date = parsedDate,
                qualifications = qualifications
            ).asSuccess()
        }
    }

    class Qualification private constructor(
        val id: QualificationId,
        val statusDetails: QualificationStatusDetails,
        val documents: List<Document>,
        val internalId: String?,
        val description: String?
    ) {
        companion object {
            private val allowedStatusDetails = QualificationStatusDetails.allowedElements
                .filter {
                    when (it) {
                        QualificationStatusDetails.ACTIVE,
                        QualificationStatusDetails.UNSUCCESSFUL -> true

                        QualificationStatusDetails.AWAITING,
                        QualificationStatusDetails.BASED_ON_HUMAN_DECISION,
                        QualificationStatusDetails.CONSIDERATION -> false
                    }
                }
                .toSet()

            fun tryCreate(
                id: String,
                statusDetails: String,
                documents: List<Document>?,
                internalId: String?,
                description: String?
            ): Result<Qualification, DataErrors> {
                val parsedId = parseQualificationId(value = id)
                    .orForwardFail { fail -> return fail }

                val statusDetailsParsed = parseEnum(
                    allowedEnums = allowedStatusDetails,
                    target = QualificationStatusDetails,
                    value = statusDetails,
                    attributeName = "statusDetails"
                )
                    .orForwardFail { fail -> return fail }
                return Qualification(
                    id = parsedId,
                    statusDetails = statusDetailsParsed,
                    description = description,
                    internalId = internalId,
                    documents = documents ?: emptyList()
                )
                    .asSuccess()
            }
        }

        class Document private constructor(
            val id: DocumentId,
            val documentType: DocumentType,
            val title: String,
            val description: String?
        ) {
            companion object {

                private val allowedDocumentType = DocumentType.allowedElements
                    .filter {
                        when (it) {
                            DocumentType.CONFLICT_OF_INTEREST,
                            DocumentType.EVALUATION_REPORTS,
                            DocumentType.NOTICE -> true
                        }
                    }
                    .toSet()

                fun tryCreate(
                    id: String,
                    documentType: String,
                    title: String,
                    description: String?
                ): Result<Document, DataErrors> {

                    val parsedDocumentId = DocumentId.parse(id)
                        ?: return DataErrors.Validation.EmptyString(name = id)
                            .asFailure()

                    val parsedDocumentType = parseEnum(
                        value = documentType,
                        attributeName = "documentType",
                        target = DocumentType,
                        allowedEnums = allowedDocumentType
                    )
                        .orForwardFail { fail -> return fail }

                    return Document(
                        id = parsedDocumentId,
                        description = description,
                        title = title,
                        documentType = parsedDocumentType
                    )
                        .asSuccess()
                }
            }
        }
    }
}
