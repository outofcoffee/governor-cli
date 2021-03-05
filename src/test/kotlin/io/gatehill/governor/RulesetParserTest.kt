package io.gatehill.governor

import io.gatehill.governor.model.rules.MandatoryPropertiesAddedRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths

class RulesetParserTest {
    private lateinit var parser: RulesetParser
    private lateinit var rulesFile: Path

    @BeforeEach
    fun configureSystemUnderTest() {
        parser = RulesetParser()
        rulesFile = Paths.get(RulesetParserTest::class.java.getResource("/test-ruleset.yaml").toURI())
    }

    @Test
    @DisplayName("Should parse ruleset")
    fun shouldParseRuleset() {
        val ruleset = parser.loadFromFile(rulesFile.toString())
        assertThat(ruleset.rules.size).isEqualTo(1)
        assertThat(ruleset.rules[0].javaClass).isEqualTo(MandatoryPropertiesAddedRule::class.java)
    }
}
