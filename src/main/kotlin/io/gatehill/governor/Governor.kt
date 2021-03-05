package io.gatehill.governor

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required

object Governor {
    private val rulesetParser = RulesetParser.defaultInstance
    private val specificationParser = SpecificationParser.defaultInstance
    private val ruleEnforcer = RuleEnforcer.defaultInstance

    @JvmStatic
    fun main(args: Array<String>) {
        val parser = ArgParser("governor")
        val currentSpecFile by parser.option(
            ArgType.String,
            shortName = "s",
            description = "OpenAPI specification file"
        ).required()
        val previousSpecFile by parser.option(
            ArgType.String,
            shortName = "p",
            description = "Previous OpenAPI specification file"
        )
        val rulesFile by parser.option(
            ArgType.String,
            shortName = "r",
            description = "Rules file"
        ).required()

        parser.parse(args)

        val ruleset = rulesetParser.loadFromFile(rulesFile)
        val currentSpec = specificationParser.loadFromFile(currentSpecFile)
        val previousSpec = previousSpecFile?.let { specificationParser.loadFromFile(it) }
        ruleEnforcer.enforce(currentSpec, previousSpec, ruleset)
    }
}
