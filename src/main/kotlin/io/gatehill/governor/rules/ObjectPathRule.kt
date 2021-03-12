package io.gatehill.governor.rules

import com.jayway.jsonpath.PathNotFoundException
import io.gatehill.governor.model.RuleInfo
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult
import io.gatehill.governor.model.rules.AbstractRule

@RuleInfo("value-at-path")
class ObjectPathRule : AbstractRule() {
    override val configClass: Class<*> = ObjectRuleConfig::class.java

    override fun test(context: EvaluationContext): EvaluationResult {
        val config = context.ruleConfig as ObjectRuleConfig

        val pathValue = try {
            context.currentSpecJsonPath!!.read<Any?>(config.path)
        } catch (e: PathNotFoundException) {
            null
        }

        return when (config.operator) {
            ObjectRuleConfig.ObjectRuleOperator.Exists -> checkExists(config, pathValue)
            ObjectRuleConfig.ObjectRuleOperator.NotExists -> checkExists(config, pathValue, true)
            ObjectRuleConfig.ObjectRuleOperator.Blank -> checkNotBlank(config, pathValue as? String, true)
            ObjectRuleConfig.ObjectRuleOperator.NotBlank -> checkNotBlank(config, pathValue as? String)
            ObjectRuleConfig.ObjectRuleOperator.EqualTo -> checkEquals(config, pathValue as? String)
            ObjectRuleConfig.ObjectRuleOperator.NotEqualTo -> checkEquals(config, pathValue as? String, true)
        }
    }

    /**
     * Check if a block/object exists.
     */
    private fun checkExists(config: ObjectRuleConfig, pathValue: Any?, invert: Boolean = false): EvaluationResult {
        return if (!invert && null != pathValue) {
            EvaluationResult(
                true,
                "value at: ${config.path} ${if (invert) "does not exist" else "exists"}"
            )
        } else {
            EvaluationResult(
                false,
                "mismatched value at: ${config.path} - expected ${if (invert) "does not exist" else "exists"}, actual: $pathValue"
            )
        }
    }

    /**
     * Check if a string is not blank.
     */
    private fun checkNotBlank(config: ObjectRuleConfig, pathValue: String?, invert: Boolean = false): EvaluationResult {
        return if (!invert && pathValue?.isNotBlank() == true) {
            EvaluationResult(
                true,
                "value at: ${config.path} is ${if (invert) "blank" else "not blank"}"
            )
        } else {
            EvaluationResult(
                false,
                "mismatched value at: ${config.path} - expected ${if (invert) "blank" else "not blank"}, actual: $pathValue"
            )
        }
    }

    /**
     * Check if a string equals the specified value.
     */
    private fun checkEquals(config: ObjectRuleConfig, pathValue: String?, invert: Boolean = false): EvaluationResult {
        return if (!invert && pathValue?.equals(config.value) == true) {
            EvaluationResult(
                true,
                "value at: ${config.path} ${if (invert) "!=" else "=="} $pathValue"
            )
        } else {
            EvaluationResult(
                false,
                "mismatched value at: ${config.path} - expected ${if (invert) "!=" else "=="} ${config.value}, actual: $pathValue"
            )
        }
    }

    data class ObjectRuleConfig(
        val path: String,
        val operator: ObjectRuleOperator = ObjectRuleOperator.Exists,
        val value: String? = null
    ) {
        enum class ObjectRuleOperator {
            Exists,
            NotExists,
            Blank,
            NotBlank,
            EqualTo,
            NotEqualTo
        }
    }
}