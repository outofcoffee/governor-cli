package io.gatehill.governor.model.rules

import io.gatehill.governor.model.config.ConfigMetadata
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult

interface Rule {
    val info: ConfigMetadata
    val configClass: Class<*>?

    fun test(context: EvaluationContext): EvaluationResult
}
