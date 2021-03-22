package io.gatehill.governor.filters

import io.gatehill.governor.SpecificationParser
import io.gatehill.governor.filter.IgnoreObjectValueFilter
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.SingleValueResult
import io.gatehill.governor.support.TestUtil
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IgnoreObjectValueFilterTest {
    private lateinit var previousSpec: OpenAPI
    private lateinit var currentSpec: OpenAPI
    private lateinit var filter: IgnoreObjectValueFilter

    @BeforeEach
    internal fun setUp() {
        filter = IgnoreObjectValueFilter()

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
    fun `should ignore result with matching object path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            previousSpec = previousSpec,
        )
        val result = SingleValueResult(false, "test result", "\$.info.license.name")
        val config = IgnoreObjectValueFilter.ObjectPathFilterConfig("\$.info.license.name")

        val include = filter.include(context, result, config)
        assertFalse(include, "Filter should ignore result")
    }

    @Test
    fun `should include result with non-matching object path`() {
        val context = EvaluationContext(
            currentSpec = currentSpec,
            previousSpec = previousSpec,
        )
        val result = SingleValueResult(false, "test result", "\$.info.license.name")
        val config = IgnoreObjectValueFilter.ObjectPathFilterConfig("\$.info.title")

        val include = filter.include(context, result, config)
        assertTrue(include, "Filter should include result")
    }
}