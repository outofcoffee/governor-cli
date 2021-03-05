package io.gatehill.governor

import io.gatehill.governor.model.Ruleset
import io.swagger.v3.oas.models.OpenAPI

class RuleEnforcer {
    fun enforce(currentSpec: OpenAPI, previousSpec: OpenAPI? = null, ruleset: Ruleset) {
        TODO("Not yet implemented")
    }

    companion object {
        val defaultInstance = RuleEnforcer()
    }
}
