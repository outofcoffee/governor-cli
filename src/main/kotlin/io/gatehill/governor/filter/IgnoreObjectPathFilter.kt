package io.gatehill.governor.filter

import io.gatehill.governor.model.config.ConfigMetadata
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult
import io.gatehill.governor.model.eval.SinglePathResult

@ConfigMetadata("ignore-object-path")
class IgnoreObjectPathFilter : AbstractFilter() {
    override val configClass = ObjectPathFilterConfig::class.java

    override fun include(context: EvaluationContext, result: EvaluationResult, config: Any?): Boolean {
        if (result is SinglePathResult && config is ObjectPathFilterConfig) {
            return result.path != config.path
        }
        return true
    }

    data class ObjectPathFilterConfig(
        val path: String
    )
}
