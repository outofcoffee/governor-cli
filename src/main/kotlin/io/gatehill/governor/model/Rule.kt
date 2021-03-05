package io.gatehill.governor.model

import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult

interface Rule {
    val info: RuleInfo

    fun test(context: EvaluationContext): EvaluationResult
}
