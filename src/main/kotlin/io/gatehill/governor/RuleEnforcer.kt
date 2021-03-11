package io.gatehill.governor

import io.gatehill.governor.model.Rule
import io.gatehill.governor.model.Ruleset
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult
import io.swagger.v3.oas.models.OpenAPI
import org.apache.logging.log4j.LogManager

class RuleEnforcer {
    private val logger = LogManager.getLogger(RuleEnforcer::class.java)

    fun enforce(currentSpec: OpenAPI, previousSpec: OpenAPI? = null, ruleset: Ruleset): Boolean {
        val results = ruleset.rules.map {
            val context = EvaluationContext(currentSpec, previousSpec, it.config)
            EvaluatedRule(
                rule = it.rule,
                result = it.rule.test(context)
            )
        }

        val passedRules = results.filter { it.result.success }
        val failedRules = results.filterNot { it.result.success }
        val passed = passedRules.joinToString("\n") { "✅   ${it.rule.info.name}: ${it.result.message ?: ""}" }

        if (results.all { it.result.success }) {
            logger.info("All rules passed (${passedRules.size}):\n$passed")
            return true

        } else {
            val failed = failedRules.joinToString("\n") { "❌   ${it.rule.info.name}: ${it.result.message ?: ""}" }

            logger.warn("Some rules failed.\n\nFailed (${failedRules.size}):\n$failed\n\nPassed (${passedRules.size}):\n$passed")
            return false
        }
    }

    companion object {
        val defaultInstance = RuleEnforcer()
    }

    data class EvaluatedRule(
        val rule: Rule,
        val result: EvaluationResult
    )
}
