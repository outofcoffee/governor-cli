package io.gatehill.governor.model.filter

data class ReifiedFilter(
    val filter: Filter,
    val config: Any? = null
)
