package io.gatehill.governor

import io.gatehill.governor.model.Ruleset
import io.gatehill.governor.model.eval.EvaluationContext
import io.swagger.v3.oas.models.OpenAPI
import org.apache.logging.log4j.LogManager

class RuleEnforcer {
    private val logger = LogManager.getLogger(RuleEnforcer::class.java)

    fun enforce(currentSpec: OpenAPI, previousSpec: OpenAPI? = null, ruleset: Ruleset) {
        val context = EvaluationContext(currentSpec, previousSpec)

        val results = ruleset.rules.map { rule -> rule to rule.test(context) }.toMap()

        if (results.all { it.value.success }) {
            logger.info("All rules evaluated successfully")
        } else {
            val failed = results.filterNot { it.value.success }
                .map { "${it.key.info.name}: ${it.value.message ?: ""}" }
                .joinToString("\n")

            logger.warn("Failing rules:\n$failed")
        }
    }

    companion object {
        val defaultInstance = RuleEnforcer()
    }
}
