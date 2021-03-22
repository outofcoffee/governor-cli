package io.gatehill.governor

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import kotlin.system.exitProcess

object Governor {
    private val rulesetParser = RulesetParser.defaultInstance
    private val filterParser = FiltersetParser.defaultInstance
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
        val filterFile by parser.option(
            ArgType.String,
            shortName = "f",
            description = "Filters file"
        )
        val verboseExitCode by parser.option(
            ArgType.Boolean,
            shortName = "x",
            description = "Return a non-zero exit code if rule evaluation fails",
        )

        parser.parse(args)

        val ruleset = rulesetParser.loadFromFile(rulesFile)
        val filters = filterFile?.let { filterParser.loadFromFile(it) }
        val currentSpec = specificationParser.loadFromFile(currentSpecFile)
        val previousSpec = previousSpecFile?.let { specificationParser.loadFromFile(it) }

        val outcome = ruleEnforcer.enforce(currentSpec, previousSpec, ruleset, filters)
        exitProcess(if (verboseExitCode == true && !outcome) 1 else 0)
    }
}
