package io.gatehill.governor.rules

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import io.gatehill.governor.SpecificationParser
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.support.TestUtil
import io.gatehill.governor.util.SerialisationUtil
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ObjectPathRuleTest {
    private lateinit var currentSpec: OpenAPI
    private lateinit var rule: ObjectPathRule
    private lateinit var currentSpecJsonPath: DocumentContext

    @BeforeEach
    internal fun setUp() {
        rule = ObjectPathRule()

        val currentSpecFile = TestUtil.findClasspathFile("/petstore_v2.yaml")
        currentSpec = SpecificationParser.defaultInstance.loadFromFile(currentSpecFile.toString())

        currentSpecJsonPath = JsonPath.parse(SerialisationUtil.jsonMapper.writeValueAsString(currentSpec))
    }

    @Test
    fun `should pass on finding existing object at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectPathRule.ObjectRuleConfig(path = "$.info"),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertTrue(result.success, "Rule should evaluate to passed")
        Assertions.assertEquals("value at: \$.info exists", result.message)
    }

    @Test
    fun `should fail on nonexistent object at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectPathRule.ObjectRuleConfig(path = "$.doesnotexist"),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertFalse(result.success, "Rule should evaluate to failed")
        Assertions.assertEquals("mismatched value at: \$.doesnotexist - expected exists, actual: null", result.message)
    }

    @Test
    fun `should pass on finding non-blank value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectPathRule.ObjectRuleConfig(
                path = "$.info.title",
                operator = ObjectPathRule.ObjectRuleConfig.ObjectRuleOperator.NotBlank
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertTrue(result.success, "Rule should evaluate to passed")
        Assertions.assertEquals("value at: \$.info.title is not blank", result.message)
    }

    @Test
    fun `should fail on non-blank value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectPathRule.ObjectRuleConfig(
                path = "$.info.title",
                operator = ObjectPathRule.ObjectRuleConfig.ObjectRuleOperator.Blank
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertFalse(result.success, "Rule should evaluate to failed")
        Assertions.assertEquals("mismatched value at: \$.info.title - expected blank, actual: Swagger Petstore", result.message)
    }

    @Test
    fun `should pass on matching value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectPathRule.ObjectRuleConfig(
                path = "$.info.title",
                operator = ObjectPathRule.ObjectRuleConfig.ObjectRuleOperator.EqualTo,
                value = "Swagger Petstore"
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertTrue(result.success, "Rule should evaluate to passed")
        Assertions.assertEquals("value at: \$.info.title == Swagger Petstore", result.message)
    }

    @Test
    fun `should fail on matching unwanted value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectPathRule.ObjectRuleConfig(
                path = "$.info.version",
                operator = ObjectPathRule.ObjectRuleConfig.ObjectRuleOperator.NotEqualTo,
                value = "1.0.0"
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertFalse(result.success, "Rule should evaluate to failed")
        Assertions.assertEquals("mismatched value at: \$.info.version - expected != 1.0.0, actual: 1.0.0", result.message)
    }
}