package com.procurement.qualification.infrastructure.handler.analyze.qualification


import com.fasterxml.jackson.annotation.JsonProperty

data class AnalyzeQualificationsForInvitationRequest(
    @param:JsonProperty("cpid") @field:JsonProperty("cpid") val cpid: String,
    @param:JsonProperty("ocid") @field:JsonProperty("ocid") val ocid: String,
    @param:JsonProperty("pmd") @field:JsonProperty("pmd") val pmd: String,
    @param:JsonProperty("country") @field:JsonProperty("country") val country: String
)