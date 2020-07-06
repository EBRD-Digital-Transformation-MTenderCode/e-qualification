package com.procurement.qualification.infrastructure.handler.create.qualification

import com.procurement.qualification.application.model.params.DoQualificationParams
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.infrastructure.fail.error.DataErrors

fun DoQualificationRequest.convert(): Result<DoQualificationParams, DataErrors> =
    DoQualificationParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        qualifications = this.qualifications
            .map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun DoQualificationRequest.Qualification.convert(): Result<DoQualificationParams.Qualification, DataErrors> =
    DoQualificationParams.Qualification.tryCreate(
        id = this.id,
        description = this.description,
        statusDetails = this.statusDetails,
        internalId = this.internalId,
        documents = this.documents
            ?.map {
                it.convert()
                    .orForwardFail { fail -> return fail }
            }
    )

fun DoQualificationRequest.Qualification.Document.convert(): Result<DoQualificationParams.Qualification.Document, DataErrors> =
    DoQualificationParams.Qualification.Document.tryCreate(
        id = this.id,
        description = this.description,
        documentType = this.documentType,
        title = this.title
    )

