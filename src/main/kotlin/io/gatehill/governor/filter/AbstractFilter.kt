package io.gatehill.governor.filter

import io.gatehill.governor.model.config.ConfigMetadata
import io.gatehill.governor.model.filter.Filter
import kotlin.reflect.full.findAnnotation

abstract class AbstractFilter : Filter {
    override val info: ConfigMetadata by lazy {
        this::class.findAnnotation()
            ?: throw IllegalStateException("Missing rule info for: ${this::class.qualifiedName}")
    }

    override val configClass: Class<*>?
        get() = throw IllegalStateException("${this::class.qualifiedName} does not have a configuration class")

    override fun toString() = "Filter[$info.name]"
}
