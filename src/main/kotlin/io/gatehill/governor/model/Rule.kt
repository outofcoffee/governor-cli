package io.gatehill.governor.model

interface Rule {
    fun test(context: ExecutionContext)
}
