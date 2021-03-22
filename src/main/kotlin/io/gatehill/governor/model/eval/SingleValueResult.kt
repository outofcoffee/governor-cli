package io.gatehill.governor.model.eval

data class SingleValueResult(
    override val success: Boolean,
    override val message: String? = null,
    val at: String
) : EvaluationResult
