package io.gatehill.governor.model.filter

import io.gatehill.governor.model.config.ConfigMetadata
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult

interface Filter {
    val info: ConfigMetadata
    val configClass: Class<*>?

    /**
     * @return `true` if in scope, otherwise `false` if should be ignored
     */
    fun include(context: EvaluationContext, result: EvaluationResult, config: Any?): Boolean
}