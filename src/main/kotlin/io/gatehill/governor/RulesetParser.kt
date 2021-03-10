package io.gatehill.governor

import com.fasterxml.jackson.module.kotlin.readValue
import io.gatehill.governor.model.ReifiedRule
import io.gatehill.governor.model.Rule
import io.gatehill.governor.model.Ruleset
import io.gatehill.governor.model.config.RulesetDef
import io.gatehill.governor.rules.ObjectPathRule
import io.gatehill.governor.rules.RequiredParametersAddedRule
import io.gatehill.governor.util.SerialisationUtil
import java.nio.file.Paths

class RulesetParser {
    private val registeredRules = listOf<Rule>(
        RequiredParametersAddedRule(),
        ObjectPathRule()
    )

    private val rules = registeredRules.map { rule -> return@map rule.info.name to rule }.toMap()

    fun loadFromFile(rulesFilePath: String): Ruleset {
        val rulesFile = Paths.get(rulesFilePath).toFile()
        if (!rulesFile.exists()) {
            throw IllegalArgumentException("Rules file: $rulesFilePath does not exist")
        }

        val rulesetDef = SerialisationUtil.yamlMapper.readValue<RulesetDef>(rulesFile)

        return Ruleset(rulesetDef.rules.map { ruleDef ->
            if (ruleDef is String) {
                ReifiedRule(
                    rule = lookupRule(ruleDef)
                )
            } else {
                val ruleWithConfig = ruleDef as Map<String, Map<String, Any>>
                val ruleName = ruleWithConfig.keys.first()
                val rule = lookupRule(ruleName)
                val configMap = ruleWithConfig[ruleName]
                ReifiedRule(
                    rule = rule,
                    config = SerialisationUtil.yamlMapper.convertValue(configMap, rule.configClass)
                )
            }
        })
    }

    private fun lookupRule(ruleName: String) =
        rules.get(ruleName) ?: throw IllegalStateException("No matching rule with name: $ruleName")

    companion object {
        val defaultInstance = RulesetParser()
    }
}
