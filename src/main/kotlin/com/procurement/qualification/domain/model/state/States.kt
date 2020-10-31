package com.procurement.qualification.domain.model.state

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails

class States(states: List<State>) : List<States.State> by states {
    data class State(
        @param:JsonProperty("status") @field:JsonProperty("status") val status: QualificationStatus,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @param:JsonProperty("statusDetails") @field:JsonProperty("statusDetails") val statusDetails: QualificationStatusDetails?
    ){
        override fun equals(other: Any?): Boolean =
            if (this === other) true
            else other is State
                && status == other.status
                && statusDetails == other.statusDetails

        override fun hashCode(): Int {
            var result = status.hashCode()
            result = 31 * result + (statusDetails?.hashCode() ?: 0)
            return result
        }
    }
}
