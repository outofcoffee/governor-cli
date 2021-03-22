package io.gatehill.governor.model.eval

data class CompositeResult(
    override val success: Boolean,
    val results: List<EvaluationResult>,
) : EvaluationResult {
    override val message: String = results.map { it.message }.joinToString()
}
