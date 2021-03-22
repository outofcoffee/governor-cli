package io.gatehill.governor.filter

import io.gatehill.governor.model.config.ConfigMetadata
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult
import io.gatehill.governor.model.eval.SingleValueResult

@ConfigMetadata("ignore-value")
class IgnoreObjectValueFilter : AbstractFilter() {
    override val configClass = ObjectPathFilterConfig::class.java

    override fun include(context: EvaluationContext, result: EvaluationResult, config: Any?): Boolean {
        if (result is SingleValueResult && config is ObjectPathFilterConfig) {
            return result.at != config.at
        }
        return true
    }

    data class ObjectPathFilterConfig(
        val at: String
    )
}
