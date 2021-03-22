package io.gatehill.governor.model.rules

data class ReifiedRule(
    val rule: Rule,
    val config: Any? = null
)
