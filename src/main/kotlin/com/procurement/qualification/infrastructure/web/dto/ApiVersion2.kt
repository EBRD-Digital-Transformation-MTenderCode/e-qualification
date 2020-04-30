package com.procurement.qualification.infrastructure.web.dto

import com.procurement.qualification.domain.functional.Result

data class ApiVersion2(val major: Int, val minor: Int, val patch: Int) {
    companion object {
        fun valueOf(version: String): ApiVersion2 {
            val elements = version.split(".")
            if (elements.isEmpty() || elements.size != 3)
                throw IllegalArgumentException("Invalid value of the api version ($version).")

            val major: Int = elements[0].toIntOrNull()
                ?: throw IllegalArgumentException("Invalid value of the api version ($version).")

            val minor: Int = elements[1].toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid value of the api version ($version).")

            val patch: Int = elements[2].toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid value of the api version ($version).")

            return ApiVersion2(major = major, minor = minor, patch = patch)
        }

        fun tryValueOf(version: String): Result<ApiVersion2, String> {
            val elements = version.split(".")
            if (elements.isEmpty() || elements.size != 3)
                return Result.failure("Invalid value of the api version ($version).")

            val major: Int = elements[0].toIntOrNull()
                ?: return Result.failure("Invalid value of the api version ($version).")

            val minor: Int = elements[1].toIntOrNull()
                ?: return Result.failure("Invalid value of the api version ($version).")

            val patch: Int = elements[2].toIntOrNull()
                ?: return Result.failure("Invalid value of the api version ($version).")

            return Result.success(ApiVersion2(major = major, minor = minor, patch = patch))
        }
    }

    override fun toString(): String = "$major.$minor.$patch"
}
