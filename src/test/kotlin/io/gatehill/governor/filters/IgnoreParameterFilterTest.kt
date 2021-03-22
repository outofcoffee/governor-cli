package io.gatehill.governor.filters

import io.gatehill.governor.SpecificationParser
import io.gatehill.governor.filter.IgnoreParameterFilter
import io.gatehill.governor.model.ParameterIdentifier
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.ParameterResult
import io.gatehill.governor.model.eval.SingleValueResult
import io.gatehill.governor.support.TestUtil
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IgnoreParameterFilterTest {
    private lateinit var previousSpec: OpenAPI
    private lateinit var currentSpec: OpenAPI
    private lateinit var filter: IgnoreParameterFilter

    @BeforeEach
    internal fun setUp() {
        filter = IgnoreParameterFilter()

        val previousSpecFile = TestUtil.findClasspathFile("/petstore_v1.yaml")
        previousSpec = SpecificationParser.defaultInstance.loadFromFile(previousSpecFile.toString())

        val currentSpecFile = TestUtil.findClasspathFile("/petstore_v2.yaml")
        currentSpec = SpecificationParser.defaultInstance.loadFromFile(currentSpecFile.toString())
    }

    @Test
    fun `should include when result is not supported`() {
        val context = EvaluationContext(
            currentSpec = currentSpec
        )
        val invalidResult = SingleValueResult(false, null, "")

        val include = filter.include(context, invalidResult, null)
        assertTrue(include, "Should return true when result is unsupported")
    }

    @Test
    fun `should ignore result with matching parameter`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            previousSpec = previousSpec,
        )
        val result = ParameterResult(false, "test result", ParameterIdentifier(
            path = "/pets",
            operation = "get",
            parameter = "size"
        ))
        val config = IgnoreParameterFilter.IgnoreParameterConfig(
            path = "/pets",
            operation = "get",
            parameter = "size"
        )

        val include = filter.include(context, result, config)
        assertFalse(include, "Filter should ignore result")
    }

    @Test
    fun `should include result with non-matching parameter`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            previousSpec = previousSpec,
        )
        val result = ParameterResult(false, "test result", ParameterIdentifier(
            path = "/pets",
            operation = "get",
            parameter = "size"
        ))
        val config = IgnoreParameterFilter.IgnoreParameterConfig(
            path = "/pets",
            operation = "get",
            parameter = "different-param"
        )

        val include = filter.include(context, result, config)
        assertTrue(include, "Filter should include result")
    }
}