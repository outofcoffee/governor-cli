package io.gatehill.governor.rules

import io.gatehill.governor.SpecificationParser
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.support.TestUtil
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RequiredParametersAddedRuleTest {
    private lateinit var previousSpec: OpenAPI
    private lateinit var currentSpec: OpenAPI
    private lateinit var rule: RequiredParametersAddedRule

    @BeforeEach
    internal fun setUp() {
        rule = RequiredParametersAddedRule()

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
    fun `should find newly added required parameters`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            previousSpec = previousSpec
        )
        val result = rule.test(context)

        assertFalse(result.success, "Rule should evaluate to failed")
        assertEquals("Required parameter 'category' in GET /pets: new in latest version", result.message)
    }
}