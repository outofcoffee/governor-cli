package io.gatehill.governor.model.rules

import io.gatehill.governor.model.Rule
import io.gatehill.governor.model.RuleInfo
import kotlin.reflect.full.findAnnotation

abstract class AbstractRule : Rule {
    override val info: RuleInfo by lazy {
        this::class.findAnnotation<RuleInfo>()
            ?: throw IllegalStateException("Missing rule info for: ${this::class.qualifiedName}")
    }

    override fun toString() = "Rule[${info.name}]"
}
