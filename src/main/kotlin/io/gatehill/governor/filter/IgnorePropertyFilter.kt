package io.gatehill.governor.filter

import io.gatehill.governor.model.config.ConfigMetadata
import io.gatehill.governor.model.PropertyIdentifier
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult
import io.gatehill.governor.model.eval.PropertyResult

@ConfigMetadata("ignore-property")
class IgnorePropertyFilter : AbstractFilter() {
    override val configClass = IgnorePropertyConfig::class.java

    override fun include(context: EvaluationContext, result: EvaluationResult, config: Any?): Boolean {
        if (result is PropertyResult && config is IgnorePropertyConfig) {
            return result.property != config
        }
        return true
    }

    class IgnorePropertyConfig(
        path: String,
        contentType: String,
        property: String
    ) : PropertyIdentifier(path, contentType, property)
}
