package com.procurement.qualification.domain.exception.coefficient.value

class CoefficientValueException(coefficientValue: String, description: String = "") :
    RuntimeException("Incorrect value of the coefficient: '$coefficientValue'. $description")
