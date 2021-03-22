package io.gatehill.governor

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import io.gatehill.governor.model.eval.CompositeResult
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult
import io.gatehill.governor.model.filter.Filterset
import io.gatehill.governor.model.rules.Rule
import io.gatehill.governor.model.rules.Ruleset
import io.gatehill.governor.util.SerialisationUtil
import io.swagger.v3.oas.models.OpenAPI
import org.apache.logging.log4j.LogManager

class RuleEnforcer {
    private val logger = LogManager.getLogger(RuleEnforcer::class.java)

    fun enforce(currentSpec: OpenAPI, previousSpec: OpenAPI? = null, ruleset: Ruleset, filterset: Filterset?): Boolean {
        // cache JSONPath context for better performance across rules
        val currentSpecJsonPath: DocumentContext by lazy {
            JsonPath.parse(SerialisationUtil.jsonMapper.writeValueAsString(currentSpec))
        }

        val rawResults = ruleset.rules.flatMap { reifiedRule ->
            val context = EvaluationContext(currentSpec, previousSpec, reifiedRule.config, currentSpecJsonPath)
            val ruleResult = reifiedRule.rule.test(context)

            flattenResults(ruleResult).map { result -> EvaluatedRule(context, reifiedRule.rule, result) }
        }

        val results = rawResults.filter { shouldIncludeResult(filterset, it) }

        val skipped = rawResults - results
        if (skipped.isEmpty()) {
            logger.debug("No results skipped")
        } else {
            val skippedResults = skipped.joinToString("\n"){ "⚪   ${it.rule.info.name}: ${it.result.message ?: ""}" }
            logger.debug("Skipped results (${skipped.size}):\n$skippedResults")
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

    private fun flattenResults(ruleResult: EvaluationResult): List<EvaluationResult> =
        if (ruleResult is CompositeResult) {
            ruleResult.results.flatMap { flattenResults(it) }
        } else {
            listOf(ruleResult)
        }

    private fun shouldIncludeResult(filterset: Filterset?, result: EvaluatedRule): Boolean =
        (filterset?.filters?.all { it.filter.include(result.context, result.result, it.config) } != false)

    companion object {
        val defaultInstance = RuleEnforcer()
    }

    private data class EvaluatedRule(
        val context: EvaluationContext,
        val rule: Rule,
        val result: EvaluationResult
    )
}
