package io.gatehill.governor.rules

import io.gatehill.governor.SpecificationParser
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.support.TestUtil
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RequiredPropertiesAddedRuleTest {
    private lateinit var previousSpec: OpenAPI
    private lateinit var currentSpec: OpenAPI
    private lateinit var rule: RequiredPropertiesAddedRule

    @BeforeEach
    internal fun setUp() {
        rule = RequiredPropertiesAddedRule()

        val previousSpecFile = TestUtil.findClasspathFile("/petstore_v1.yaml")
        previousSpec = SpecificationParser.defaultInstance.loadFromFile(previousSpecFile.toString())

        val currentSpecFile = TestUtil.findClasspathFile("/petstore_v2.yaml")
        currentSpec = SpecificationParser.defaultInstance.loadFromFile(currentSpecFile.toString())
    }

    @Test
    fun `should throw exception when missing previous spec`() {
        val context = EvaluationContext(
            currentSpec = currentSpec
        )

        assertThrows(IllegalStateException::class.java) { rule.test(context) }
    }

    @Test
    fun `should find newly added required properties`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            previousSpec = previousSpec
        )
        val result = rule.test(context)

        assertFalse(result.success, "Rule should evaluate to failed")
        assertEquals("Required property 'age' in POST /pets request (application/json): newly required in latest version", result.message)
    }
}