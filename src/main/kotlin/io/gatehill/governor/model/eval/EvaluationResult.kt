package io.gatehill.governor.model.eval

data class EvaluationResult(
    val success: Boolean,
    val message: String? = null
)
