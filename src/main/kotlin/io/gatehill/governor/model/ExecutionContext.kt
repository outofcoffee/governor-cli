package io.gatehill.governor.model

import io.swagger.v3.oas.models.OpenAPI

data class ExecutionContext(
    val currentSpec: OpenAPI,
    val previousSpec: OpenAPI? = null,
    val ruleset: Ruleset
)
