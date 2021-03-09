package io.gatehill.governor.model.eval

import io.swagger.v3.oas.models.OpenAPI

data class EvaluationContext(
    val currentSpec: OpenAPI,
    val previousSpec: OpenAPI? = null
)
