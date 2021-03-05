package io.gatehill.governor.model.rules

import io.gatehill.governor.model.eval.EvaluationResult
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.Rule
import io.gatehill.governor.model.RuleInfo

@RuleInfo("mandatory-properties-added")
class MandatoryPropertiesAddedRule : AbstractRule() {
    override fun test(context: EvaluationContext): EvaluationResult {
        // TODO implement me
        return EvaluationResult(false)
    }
}
