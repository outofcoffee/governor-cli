package io.gatehill.governor

import io.gatehill.governor.rules.RequiredParametersAddedRule
import io.gatehill.governor.support.TestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Path

class RulesetParserTest {
    private lateinit var parser: RulesetParser
    private lateinit var rulesFile: Path

    @BeforeEach
    fun configureSystemUnderTest() {
        parser = RulesetParser()
        rulesFile = TestUtil.findClasspathFile("/test-ruleset.yaml")
    }

    @Test
    @DisplayName("Should parse ruleset")
    fun `should parse ruleset`() {
        val ruleset = parser.loadFromFile(rulesFile.toString())
        assertThat(ruleset.rules.size).isEqualTo(1)
        assertThat(ruleset.rules[0].javaClass).isEqualTo(RequiredParametersAddedRule::class.java)
    }
}
