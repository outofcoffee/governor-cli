package io.gatehill.governor.rules

import io.gatehill.governor.SpecificationParser
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.support.TestUtil
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ObjectPathRuleTest {
    private lateinit var currentSpec: OpenAPI
    private lateinit var rule: ObjectPathRule

    @BeforeEach
    internal fun setUp() {
        rule = ObjectPathRule()

        val currentSpecFile = TestUtil.findClasspathFile("/petstore_v2.yaml")
        currentSpec = SpecificationParser.defaultInstance.loadFromFile(currentSpecFile.toString())
    }

    @Test
    fun `should pass on populated value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectPathRule.ObjectRuleConfig(path = "$.info.title")
        )
        val result = rule.test(context)

        Assertions.assertTrue(result.success, "Rule should evaluate to passed")
        Assertions.assertNull(result.message, "Result message should be empty")
    }

    @Test
    fun `should fail on unpopulated value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectPathRule.ObjectRuleConfig(path = "$.info.location")
        )
        val result = rule.test(context)

        Assertions.assertFalse(result.success, "Rule should evaluate to failed")
        Assertions.assertNotNull(result.message, "Result message should be populated")
        Assertions.assertEquals("blank value at: \$.info.location", result.message)
    }
}