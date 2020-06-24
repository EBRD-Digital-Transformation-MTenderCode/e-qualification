package com.procurement.qualification.domain.model.state

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.qualification.domain.enums.QualificationStatus
import com.procurement.qualification.domain.enums.QualificationStatusDetails

class States(states: List<State>) : List<States.State> by states {
    data class State(
        @param:JsonProperty("status") @field:JsonProperty("status") val status: QualificationStatus,
        @param:JsonProperty("statusDetails") @field:JsonProperty("statusDetails") val statusDetails: QualificationStatusDetails
    )
}
