package io.gatehill.governor.filter

import io.gatehill.governor.model.config.ConfigMetadata
import io.gatehill.governor.model.ParameterIdentifier
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult
import io.gatehill.governor.model.eval.ParameterResult

@ConfigMetadata("ignore-parameter")
class IgnoreParameterFilter : AbstractFilter() {
    override val configClass = IgnoreParameterConfig::class.java

    override fun include(context: EvaluationContext, result: EvaluationResult, config: Any?): Boolean {
        if (result is ParameterResult && config is IgnoreParameterConfig) {
            return result.parameter != config
        }
        return true
    }

    class IgnoreParameterConfig(
        path: String,
        operation: String,
        parameter: String
    ) : ParameterIdentifier(path, operation, parameter)
}
