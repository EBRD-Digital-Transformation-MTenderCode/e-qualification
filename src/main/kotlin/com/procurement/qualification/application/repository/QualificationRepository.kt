package com.procurement.qualification.application.repository

import com.procurement.qualification.domain.functional.MaybeFail
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.qualification.Qualification
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.infrastructure.fail.Fail

interface QualificationRepository {

    fun findBy(cpid: Cpid, ocid: Ocid): Result<List<Qualification>, Fail.Incident>

    fun findBy(cpid: Cpid, ocid: Ocid, qualificationId: QualificationId): Result<Qualification?, Fail.Incident>

    fun findBy(
        cpid: Cpid, ocid: Ocid, qualificationIds: List<QualificationId>
    ): Result<List<Qualification>, Fail.Incident>

    fun save(cpid: Cpid, ocid: Ocid, qualification: Qualification): MaybeFail<Fail.Incident>

    fun saveAll(cpid: Cpid, ocid: Ocid, qualifications: List<Qualification>): MaybeFail<Fail.Incident>

    fun updateAll(cpid: Cpid, ocid: Ocid, qualifications: List<Qualification>): MaybeFail<Fail.Incident>
}
