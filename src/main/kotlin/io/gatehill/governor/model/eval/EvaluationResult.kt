package io.gatehill.governor.model.eval

interface EvaluationResult {
    val success: Boolean
    val message: String?
}