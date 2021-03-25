package io.gatehill.governor.rules

import io.gatehill.governor.model.config.ConfigMetadata
import io.gatehill.governor.model.rules.Rule
import kotlin.reflect.full.findAnnotation

abstract class AbstractRule : Rule {
    override val info: ConfigMetadata by lazy {
        this::class.findAnnotation()
            ?: throw IllegalStateException("Missing rule info for: ${this::class.qualifiedName}")
    }

    override val configClass: Class<*>?
        get() = throw IllegalStateException("${this::class.qualifiedName} does not have a configuration class")

    override fun toString() = "Rule[${info.name}]"
}
