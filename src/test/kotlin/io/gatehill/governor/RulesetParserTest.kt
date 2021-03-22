package io.gatehill.governor

import io.gatehill.governor.rules.ObjectValueRule
import io.gatehill.governor.rules.RequiredParametersAddedRule
import io.gatehill.governor.support.TestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
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
    fun `should parse ruleset`() {
        val ruleset = parser.loadFromFile(rulesFile.toString())
        assertThat(ruleset.rules.size).isEqualTo(2)

        assertThat(ruleset.rules[0].rule.javaClass).isEqualTo(RequiredParametersAddedRule::class.java)
        assertNull(ruleset.rules[0].config?.javaClass, "First rule should have empty config")

        assertThat(ruleset.rules[1].rule.javaClass).isEqualTo(ObjectValueRule::class.java)
        assertThat(ruleset.rules[1].config?.javaClass).isEqualTo(ObjectValueRule.ObjectRuleConfig::class.java)
    }
}
