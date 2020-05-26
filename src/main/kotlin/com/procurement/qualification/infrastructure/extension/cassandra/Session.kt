package com.procurement.qualification.infrastructure.extension.cassandra

import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Session
import com.procurement.qualification.domain.functional.Result
import com.procurement.qualification.domain.functional.Result.Companion.failure
import com.procurement.qualification.domain.functional.Result.Companion.success
import com.procurement.qualification.infrastructure.fail.Fail

fun BoundStatement.tryExecute(session: Session): Result<ResultSet, Fail.Incident.Database.Interaction> = try {
    success(session.execute(this))
} catch (expected: Exception) {
    failure(Fail.Incident.Database.Interaction(exception = expected))
}