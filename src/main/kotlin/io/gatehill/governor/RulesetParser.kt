package io.gatehill.governor

import io.gatehill.governor.model.Ruleset
import java.nio.file.Paths

class RulesetParser {
    fun loadFromFile(rulesFilePath: String): Ruleset {
        val rulesFile = Paths.get(rulesFilePath).toFile()
        if (!rulesFile.exists()) {
            throw IllegalArgumentException("Rules file: ${rulesFilePath} does not exist")
        }

        return Ruleset(emptyList())
    }

    companion object {
        val defaultInstance = RulesetParser()
    }
}
