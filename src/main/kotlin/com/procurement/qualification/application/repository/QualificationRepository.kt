package com.procurement.qualification.application.repository

import com.procurement.qualification.domain.functional.MaybeFail
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.model.Cpid
import com.procurement.qualification.domain.model.Ocid
import com.procurement.qualification.domain.model.qualification.QualificationId
import com.procurement.qualification.infrastructure.fail.Fail
import com.procurement.qualification.infrastructure.model.entity.QualificationEntity

interface QualificationRepository {

    fun findBy(cpid: Cpid, ocid: Ocid): Result<List<QualificationEntity>, Fail.Incident>

    fun findBy(cpid: Cpid, ocid: Ocid,qualificationId: QualificationId): Result<QualificationEntity?, Fail.Incident>

    fun save(entity: QualificationEntity): MaybeFail<Fail.Incident>

    fun saveAll(entities: List<QualificationEntity>): MaybeFail<Fail.Incident>

    fun updateAll(entities: List<QualificationEntity>): MaybeFail<Fail.Incident>
}
