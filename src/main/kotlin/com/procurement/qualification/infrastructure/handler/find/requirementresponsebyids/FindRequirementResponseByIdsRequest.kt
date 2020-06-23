package com.procurement.qualification.infrastructure.handler.find.requirementresponsebyids


import com.fasterxml.jackson.annotation.JsonProperty

data class FindRequirementResponseByIdsRequest(
    @field:JsonProperty("requirementResponseIds") @param:JsonProperty("requirementResponseIds") val requirementResponseIds: List<String>,
    @field:JsonProperty("qualificationId") @param:JsonProperty("qualificationId") val qualificationId: String,
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String
)
