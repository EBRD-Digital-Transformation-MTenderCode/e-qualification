package com.procurement.qualification.infrastructure.model.dto.start.qualificationperiod

import com.procurement.qualification.infrastructure.handler.start.qualificationperiod.StartQualificationPeriodResult
import com.procurement.qualification.infrastructure.model.dto.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class StartQualificationPeriodResultTest : AbstractDTOTestBase<StartQualificationPeriodResult>(
    StartQualificationPeriodResult::class.java
) {

    @Test
    fun full(){
        testBindingAndMapping(pathToJsonFile = "json/dto/start/qualificationperiod/start_qualification_period_result_full.json")
    }

}