package io.gatehill.governor.model.eval

import io.gatehill.governor.model.PropertyIdentifier

data class PropertyResult(
    override val success: Boolean,
    override val message: String? = null,
    val property: PropertyIdentifier
) : EvaluationResult
