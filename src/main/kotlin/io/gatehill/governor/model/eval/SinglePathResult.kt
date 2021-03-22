package io.gatehill.governor.model.eval

data class SinglePathResult(
    override val success: Boolean,
    override val message: String? = null,
    val path: String
) : EvaluationResult
