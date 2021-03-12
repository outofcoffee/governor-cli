package io.gatehill.governor.model.eval

import com.jayway.jsonpath.DocumentContext
import io.swagger.v3.oas.models.OpenAPI

data class EvaluationContext(
    val currentSpec: OpenAPI,
    val previousSpec: OpenAPI? = null,
    val ruleConfig: Any? = null,

    /**
     * Allows JSONPath queries against the current specification.
     */
    val currentSpecJsonPath: DocumentContext? = null
)
