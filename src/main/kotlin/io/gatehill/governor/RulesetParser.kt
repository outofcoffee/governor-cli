package io.gatehill.governor

import com.fasterxml.jackson.module.kotlin.readValue
import io.gatehill.governor.model.rules.ReifiedRule
import io.gatehill.governor.model.rules.Rule
import io.gatehill.governor.model.rules.Ruleset
import io.gatehill.governor.model.config.RulesetDef
import io.gatehill.governor.rules.ObjectValueRule
import io.gatehill.governor.rules.RequiredParametersAddedRule
import io.gatehill.governor.rules.RequiredPropertiesAddedRule
import io.gatehill.governor.util.SerialisationUtil
import java.nio.file.Paths

class RulesetParser {
    private val registeredRules = listOf<Rule>(
        RequiredParametersAddedRule(),
        RequiredPropertiesAddedRule(),
        ObjectValueRule()
    )

    private val rules = registeredRules.map { rule -> return@map rule.info.name to rule }.toMap()

    @Suppress("UNCHECKED_CAST")
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
        rules[ruleName] ?: throw IllegalStateException("No matching rule with name: $ruleName")

    companion object {
        val defaultInstance = RulesetParser()
    }
}
