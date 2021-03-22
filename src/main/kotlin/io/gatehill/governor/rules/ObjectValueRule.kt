package io.gatehill.governor.rules

import com.jayway.jsonpath.PathNotFoundException
import io.gatehill.governor.model.config.ConfigMetadata
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult
import io.gatehill.governor.model.eval.SingleValueResult
import java.lang.IllegalStateException

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
            ObjectRuleConfig.ObjectRuleOperator.Blank -> checkBlank(config, pathValue as? String)
            ObjectRuleConfig.ObjectRuleOperator.NotBlank -> checkBlank(config, pathValue as? String, true)
            ObjectRuleConfig.ObjectRuleOperator.EqualTo -> checkEquals(config, pathValue as? String)
            ObjectRuleConfig.ObjectRuleOperator.NotEqualTo -> checkEquals(config, pathValue as? String, true)
            ObjectRuleConfig.ObjectRuleOperator.Contains -> checkContains(config, pathValue as? String)
            ObjectRuleConfig.ObjectRuleOperator.NotContains -> checkContains(config, pathValue as? String, true)
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
     * Check if a string is blank.
     */
    private fun checkBlank(config: ObjectRuleConfig, pathValue: String?, invert: Boolean = false): SingleValueResult {
        return if (!invert == pathValue.isNullOrBlank()) {
            SingleValueResult(
                true,
                "value at: ${config.at} is ${if (invert) "not blank" else "blank"}",
                config.at
            )
        } else {
            SingleValueResult(
                false,
                "mismatched value at: ${config.at} - expected ${if (invert) "not blank" else "blank"}, actual: $pathValue",
                config.at
            )
        }
    }

    /**
     * Check if a string equals the specified value.
     */
    private fun checkEquals(config: ObjectRuleConfig, pathValue: String?, invert: Boolean = false): SingleValueResult {
        if (null == config.value) {
            throw IllegalStateException("Missing configuration value for equality check at: ${config.at}")
        }
        return if (!invert == pathValue?.equals(config.value)) {
            SingleValueResult(
                true,
                "value at: ${config.at} ${if (invert) "!=" else "=="} ${config.value}",
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

    /**
     * Check if a string contains the specified value.
     */
    private fun checkContains(config: ObjectRuleConfig, pathValue: String?, invert: Boolean = false): SingleValueResult {
        if (null == config.value) {
            throw IllegalStateException("Missing configuration value for contains check at: ${config.at}")
        }
        return if (!invert == pathValue?.contains(config.value)) {
            SingleValueResult(
                true,
                "value at: ${config.at} ${if (invert) "does not contain" else "contains"} ${config.value}",
                config.at
            )
        } else {
            SingleValueResult(
                false,
                "mismatched value at: ${config.at} - expected ${if (invert) "does not contain" else "contains"} ${config.value}, actual: $pathValue",
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
            NotEqualTo,
            Contains,
            NotContains
        }
    }
}