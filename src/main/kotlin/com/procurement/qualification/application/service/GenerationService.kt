package com.procurement.qualification.application.service

import com.procurement.qualification.domain.model.Token
import com.procurement.qualification.domain.model.qualification.QualificationId
import org.springframework.stereotype.Service
import java.util.*

interface GenerationService {
    fun generateToken(): Token

    fun generateQualificationId(): QualificationId
}

@Service
class GenerationServiceImpl() : GenerationService {
    override fun generateToken(): Token = generateRandomUUID()

    override fun generateQualificationId(): QualificationId = QualificationId.generate()

    private fun generateRandomUUID(): UUID = UUID.randomUUID()
}
