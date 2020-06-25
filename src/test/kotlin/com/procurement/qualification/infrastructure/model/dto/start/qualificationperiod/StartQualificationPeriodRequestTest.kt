package com.procurement.qualification.infrastructure.model.dto.start.qualificationperiod

import com.procurement.qualification.infrastructure.handler.start.qualificationperiod.StartQualificationPeriodRequest
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class StartQualificationPeriodRequestTest : AbstractDTOTestBase<StartQualificationPeriodRequest>(
    StartQualificationPeriodRequest::class.java
) {

    @Test
    fun full(){
        testBindingAndMapping(pathToJsonFile = "json/dto/start/qualificationperiod/start_qualification_period_params_full.json")
    }

}