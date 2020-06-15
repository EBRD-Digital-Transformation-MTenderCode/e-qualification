package com.procurement.qualification.infrastructure.handler.determine.nextforqualification

import com.fasterxml.jackson.annotation.JsonProperty

data class DetermineNextsForQualificationRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("otherCriteria") @param:JsonProperty("otherCriteria") val otherCriteria: OtherCriteria,
    @field:JsonProperty("submissions") @param:JsonProperty("submissions") val submissions: List<Submission>
) {

    data class OtherCriteria(
        @field:JsonProperty("reductionCriteria") @param:JsonProperty("reductionCriteria") val reductionCriteria: String,
        @field:JsonProperty("qualificationSystemMethods") @param:JsonProperty("qualificationSystemMethods") val qualificationSystemMethods: List<String>
    )

    data class Submission(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("date") @param:JsonProperty("date") val date: String
    )
}
