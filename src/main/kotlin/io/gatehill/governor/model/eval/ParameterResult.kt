package io.gatehill.governor.model.eval

import io.gatehill.governor.model.ParameterIdentifier

data class ParameterResult(
    override val success: Boolean,
    override val message: String? = null,
    val parameter: ParameterIdentifier
) : EvaluationResult
