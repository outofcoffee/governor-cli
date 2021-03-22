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

internal class ObjectValueRuleTest {
    private lateinit var currentSpec: OpenAPI
    private lateinit var rule: ObjectValueRule
    private lateinit var currentSpecJsonPath: DocumentContext

    @BeforeEach
    internal fun setUp() {
        rule = ObjectValueRule()

        val currentSpecFile = TestUtil.findClasspathFile("/petstore_v2.yaml")
        currentSpec = SpecificationParser.defaultInstance.loadFromFile(currentSpecFile.toString())

        currentSpecJsonPath = JsonPath.parse(SerialisationUtil.jsonMapper.writeValueAsString(currentSpec))
    }

    @Test
    fun `should pass on finding existing object at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectValueRule.ObjectRuleConfig(at = "$.info"),
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
            ruleConfig = ObjectValueRule.ObjectRuleConfig(at = "$.doesnotexist"),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertFalse(result.success, "Rule should evaluate to failed")
        Assertions.assertEquals("mismatched value at: \$.doesnotexist - expected exists, actual: null", result.message)
    }

    @Test
    fun `should pass on finding blank value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectValueRule.ObjectRuleConfig(
                at = "$.contact.name",
                operator = ObjectValueRule.ObjectRuleConfig.ObjectRuleOperator.Blank
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertTrue(result.success, "Rule should evaluate to passed")
        Assertions.assertEquals("value at: \$.contact.name is blank", result.message)
    }

    @Test
    fun `should fail on finding non-blank value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectValueRule.ObjectRuleConfig(
                at = "$.info.title",
                operator = ObjectValueRule.ObjectRuleConfig.ObjectRuleOperator.Blank
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertFalse(result.success, "Rule should evaluate to failed")
        Assertions.assertEquals("mismatched value at: \$.info.title - expected blank, actual: Swagger Petstore", result.message)
    }

    @Test
    fun `should pass on finding non-blank value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectValueRule.ObjectRuleConfig(
                at = "$.info.title",
                operator = ObjectValueRule.ObjectRuleConfig.ObjectRuleOperator.NotBlank
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertTrue(result.success, "Rule should evaluate to passed")
        Assertions.assertEquals("value at: \$.info.title is not blank", result.message)
    }

    @Test
    fun `should fail on finding blank value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectValueRule.ObjectRuleConfig(
                at = "$.info.title",
                operator = ObjectValueRule.ObjectRuleConfig.ObjectRuleOperator.NotBlank
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertTrue(result.success, "Rule should evaluate to passed")
        Assertions.assertEquals("value at: \$.info.title is not blank", result.message)
    }

    @Test
    fun `should pass on matching value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectValueRule.ObjectRuleConfig(
                at = "$.info.title",
                operator = ObjectValueRule.ObjectRuleConfig.ObjectRuleOperator.EqualTo,
                value = "Swagger Petstore"
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertTrue(result.success, "Rule should evaluate to passed")
        Assertions.assertEquals("value at: \$.info.title == Swagger Petstore", result.message)
    }

    @Test
    fun `should fail on not matching value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectValueRule.ObjectRuleConfig(
                at = "$.info.title",
                operator = ObjectValueRule.ObjectRuleConfig.ObjectRuleOperator.EqualTo,
                value = "Another title"
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertFalse(result.success, "Rule should evaluate to passed")
        Assertions.assertEquals("mismatched value at: \$.info.title - expected == Another title, actual: Swagger Petstore", result.message)
    }

    @Test
    fun `should pass on not matching value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectValueRule.ObjectRuleConfig(
                at = "$.info.version",
                operator = ObjectValueRule.ObjectRuleConfig.ObjectRuleOperator.NotEqualTo,
                value = "2.0.0"
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertTrue(result.success, "Rule should evaluate to passed")
        Assertions.assertEquals("value at: \$.info.version != 2.0.0", result.message)
    }

    @Test
    fun `should fail on matching unwanted value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectValueRule.ObjectRuleConfig(
                at = "$.info.version",
                operator = ObjectValueRule.ObjectRuleConfig.ObjectRuleOperator.NotEqualTo,
                value = "1.0.0"
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertFalse(result.success, "Rule should evaluate to failed")
        Assertions.assertEquals("mismatched value at: \$.info.version - expected != 1.0.0, actual: 1.0.0", result.message)
    }

    @Test
    fun `should pass when containing value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectValueRule.ObjectRuleConfig(
                at = "$.info.title",
                operator = ObjectValueRule.ObjectRuleConfig.ObjectRuleOperator.Contains,
                value = "Petstore"
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertTrue(result.success, "Rule should evaluate to passed")
        Assertions.assertEquals("value at: \$.info.title contains Petstore", result.message)
    }

    @Test
    fun `should fail when not containing value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectValueRule.ObjectRuleConfig(
                at = "$.info.title",
                operator = ObjectValueRule.ObjectRuleConfig.ObjectRuleOperator.Contains,
                value = "Not found"
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertFalse(result.success, "Rule should evaluate to failed")
        Assertions.assertEquals("mismatched value at: \$.info.title - expected contains Not found, actual: Swagger Petstore", result.message)
    }

    @Test
    fun `should pass when does not contain value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectValueRule.ObjectRuleConfig(
                at = "$.servers[0].url",
                operator = ObjectValueRule.ObjectRuleConfig.ObjectRuleOperator.NotContains,
                value = "example.com"
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertTrue(result.success, "Rule should evaluate to passed")
        Assertions.assertEquals("value at: \$.servers[0].url does not contain example.com", result.message)
    }

    @Test
    fun `should fail when containing unwanted value at path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            ruleConfig = ObjectValueRule.ObjectRuleConfig(
                at = "$.servers[0].url",
                operator = ObjectValueRule.ObjectRuleConfig.ObjectRuleOperator.NotContains,
                value = "swagger.io"
            ),
            currentSpecJsonPath = currentSpecJsonPath
        )
        val result = rule.test(context)

        Assertions.assertFalse(result.success, "Rule should evaluate to failed")
        Assertions.assertEquals("mismatched value at: \$.servers[0].url - expected does not contain swagger.io, actual: http://petstore.swagger.io/v1", result.message)
    }
}