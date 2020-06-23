package com.procurement.qualification.application.service

import com.procurement.qualification.domain.model.Token
import org.springframework.stereotype.Service
import java.util.*

interface GenerationService {
    fun generateToken(): Token
}

@Service
class GenerationServiceImpl() : GenerationService {
    override fun generateToken(): Token = generateRandomUUID()

    private fun generateRandomUUID(): UUID = UUID.randomUUID()
}
