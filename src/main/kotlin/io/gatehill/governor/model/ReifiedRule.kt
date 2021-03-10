package io.gatehill.governor.model

data class ReifiedRule(
    val rule: Rule,
    val config: Any? = null
)
