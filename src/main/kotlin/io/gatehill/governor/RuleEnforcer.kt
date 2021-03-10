package io.gatehill.governor

import io.gatehill.governor.model.Ruleset
import io.gatehill.governor.model.eval.EvaluationContext
import io.swagger.v3.oas.models.OpenAPI
import org.apache.logging.log4j.LogManager

class RuleEnforcer {
    private val logger = LogManager.getLogger(RuleEnforcer::class.java)

    fun enforce(currentSpec: OpenAPI, previousSpec: OpenAPI? = null, ruleset: Ruleset): Boolean {
        val results = ruleset.rules.map {
            val context = EvaluationContext(currentSpec, previousSpec, it.config)
            it.rule to it.rule.test(context)
        }.toMap()

        val passedRules = results.filter { it.value.success }
        val failedRules = results.filterNot { it.value.success }
        val passed = passedRules.map { "✅   ${it.key.info.name}" }.joinToString("\n")

        if (results.all { it.value.success }) {
            logger.info("All rules passed (${passedRules.size}):\n$passed")
            return true

        } else {
            val failed = failedRules.map { "❌   ${it.key.info.name}: ${it.value.message ?: ""}" }
                .joinToString("\n")

            logger.warn("Some rules failed.\n\nFailed (${failedRules.size}):\n$failed\n\nPassed (${passedRules.size}):\n$passed")
            return false
        }
    }

    companion object {
        val defaultInstance = RuleEnforcer()
    }
}
