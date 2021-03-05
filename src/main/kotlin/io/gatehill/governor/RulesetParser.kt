package io.gatehill.governor

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.gatehill.governor.model.Rule
import io.gatehill.governor.model.Ruleset
import io.gatehill.governor.model.config.RulesetDef
import io.gatehill.governor.model.rules.MandatoryPropertiesAddedRule
import java.nio.file.Paths

class RulesetParser {
    private val registeredRules = listOf<Rule>(
        MandatoryPropertiesAddedRule()
    )

    private val rules = registeredRules.map { rule -> return@map rule.info.name to rule }.toMap()

    private val yamlMapper = YAMLMapper().registerKotlinModule()

    fun loadFromFile(rulesFilePath: String): Ruleset {
        val rulesFile = Paths.get(rulesFilePath).toFile()
        if (!rulesFile.exists()) {
            throw IllegalArgumentException("Rules file: ${rulesFilePath} does not exist")
        }

        val rulesetDef = yamlMapper.readValue<RulesetDef>(rulesFile)
        return Ruleset(rulesetDef.rules.map {
            rules.get(it) ?: throw IllegalStateException("No matching rule with name: ${it}")
        })
    }

    companion object {
        val defaultInstance = RulesetParser()
    }
}
