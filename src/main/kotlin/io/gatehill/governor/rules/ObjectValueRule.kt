package io.gatehill.governor.rules

import com.jayway.jsonpath.PathNotFoundException
import io.gatehill.governor.model.config.ConfigMetadata
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult
import io.gatehill.governor.model.eval.SingleValueResult

@ConfigMetadata("check-value")
class ObjectValueRule : AbstractRule() {
    override val configClass: Class<*> = ObjectRuleConfig::class.java

    override fun test(context: EvaluationContext): EvaluationResult {
        val config = context.ruleConfig as ObjectRuleConfig

        val pathValue = try {
            context.currentSpecJsonPath!!.read<Any?>(config.at)
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
    private fun checkExists(config: ObjectRuleConfig, pathValue: Any?, invert: Boolean = false): SingleValueResult {
        return if (!invert && null != pathValue) {
            SingleValueResult(
                true,
                "value at: ${config.at} ${if (invert) "does not exist" else "exists"}",
                config.at
            )
        } else {
            SingleValueResult(
                false,
                "mismatched value at: ${config.at} - expected ${if (invert) "does not exist" else "exists"}, actual: $pathValue",
                config.at
            )
        }
    }

    /**
     * Check if a string is not blank.
     */
    private fun checkNotBlank(config: ObjectRuleConfig, pathValue: String?, invert: Boolean = false): SingleValueResult {
        return if (!invert && pathValue?.isNotBlank() == true) {
            SingleValueResult(
                true,
                "value at: ${config.at} is ${if (invert) "blank" else "not blank"}",
                config.at
            )
        } else {
            SingleValueResult(
                false,
                "mismatched value at: ${config.at} - expected ${if (invert) "blank" else "not blank"}, actual: $pathValue",
                config.at
            )
        }
    }

    /**
     * Check if a string equals the specified value.
     */
    private fun checkEquals(config: ObjectRuleConfig, pathValue: String?, invert: Boolean = false): SingleValueResult {
        return if (!invert && pathValue?.equals(config.value) == true) {
            SingleValueResult(
                true,
                "value at: ${config.at} ${if (invert) "!=" else "=="} $pathValue",
                config.at
            )
        } else {
            SingleValueResult(
                false,
                "mismatched value at: ${config.at} - expected ${if (invert) "!=" else "=="} ${config.value}, actual: $pathValue",
                config.at
            )
        }
    }

    data class ObjectRuleConfig(
        val at: String,
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