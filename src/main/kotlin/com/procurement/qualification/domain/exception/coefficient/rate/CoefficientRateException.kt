package com.procurement.qualification.domain.exception.coefficient.rate

class CoefficientRateException(coefficient: String, description: String = "") :
    RuntimeException("Incorrect coefficient: '$coefficient'. $description")
