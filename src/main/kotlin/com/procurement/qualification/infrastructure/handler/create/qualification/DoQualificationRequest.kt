package com.procurement.qualification.infrastructure.handler.create.qualification


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class DoQualificationRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("date") @param:JsonProperty("date") val date: String,
    @field:JsonProperty("qualifications") @param:JsonProperty("qualifications") val qualifications: List<Qualification>
) {
    data class Qualification(
        @field:JsonProperty("id")  @param:JsonProperty("id") val id: String,
        @field:JsonProperty("statusDetails")  @param:JsonProperty("statusDetails") val statusDetails: String,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("documents")  @param:JsonProperty("documents") val documents: List<Document>?,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("internalId")  @param:JsonProperty("internalId") val internalId: String?,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("description")  @param:JsonProperty("description") val description: String?
    ) {
        data class Document(
            @field:JsonProperty("id")  @param:JsonProperty("id") val id: String,
            @field:JsonProperty("documentType")  @param:JsonProperty("documentType") val documentType: String,
            @field:JsonProperty("title")  @param:JsonProperty("title") val title: String,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("description")  @param:JsonProperty("description") val description: String?
        )
    }
}