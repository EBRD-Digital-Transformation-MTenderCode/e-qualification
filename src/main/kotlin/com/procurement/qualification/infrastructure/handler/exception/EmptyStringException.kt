package com.procurement.qualification.infrastructure.handler.exception

class EmptyStringException(val attributeName: String) : RuntimeException(attributeName)