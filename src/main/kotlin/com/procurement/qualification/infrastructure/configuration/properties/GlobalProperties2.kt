package com.procurement.qualification.infrastructure.configuration.properties

import com.procurement.qualification.infrastructure.io.orThrow
import com.procurement.qualification.infrastructure.web.dto.ApiVersion2
import java.util.*

object GlobalProperties2 {
    val service = Service()

    object App {
        val apiVersion = ApiVersion2(major = 1, minor = 0, patch = 0)
    }

    class Service {
        val id: String = ""  //TODO()
        val name: String = "e-qualification"
        val version: String = loadVersion()

        private fun loadVersion(): String {
            val gitProps: Properties = try {
                GlobalProperties2::class.java.getResourceAsStream("/git.properties")
                    .use { stream ->
                        Properties().apply { load(stream) }
                    }
            } catch (expected: Exception) {
                throw IllegalStateException(expected)
            }
            return gitProps.orThrow("git.commit.id.abbrev")
        }
    }
}
