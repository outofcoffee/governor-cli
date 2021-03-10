package io.gatehill.governor.rules

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import io.gatehill.governor.model.RuleInfo
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult
import io.gatehill.governor.model.rules.AbstractRule
import io.gatehill.governor.util.SerialisationUtil

@RuleInfo("object-path")
class ObjectPathRule : AbstractRule() {
    override val configClass: Class<*> = ObjectRuleConfig::class.java

    override fun test(context: EvaluationContext): EvaluationResult {
        val config = context.ruleConfig as ObjectRuleConfig
        val specJson = SerialisationUtil.jsonMapper.writeValueAsString(context.currentSpec)
        val documentContext = JsonPath.parse(specJson)

        return try {
            val pathValue = documentContext.read<String?>(config.path)
            EvaluationResult(pathValue?.isNotBlank() == true)
        } catch (e: PathNotFoundException) {
            EvaluationResult(false, "blank value at: ${config.path}")
        }
    }

    data class ObjectRuleConfig(val path: String)
}