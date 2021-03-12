package io.gatehill.governor.rules

import io.gatehill.governor.model.RuleInfo
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult
import io.gatehill.governor.model.rules.AbstractRule
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.RequestBody
import java.lang.UnsupportedOperationException

@RuleInfo("required-properties-added")
class RequiredPropertiesAddedRule : AbstractRule() {
    override fun test(context: EvaluationContext): EvaluationResult {
        context.previousSpec ?: throw IllegalStateException("No previous OpenAPI specification provided for evaluation")
        val previousSpecPaths = context.previousSpec.paths

        val newlyRequired = mutableListOf<String>()

        context.currentSpec.paths.forEach { path ->
            previousSpecPaths[path.key]?.let { previousSpecPath ->
                // path exists in both spec versions
                path.value.readOperationsMap().map { it.toPair() }.forEach { opEntry ->
                    newlyRequired += checkPathOperations(path.key, opEntry, previousSpecPath)
                }

            } ?: run {
                // path is new in latest version
                path.value.readOperationsMap().map { it.toPair() }.forEach { opEntry ->
                    newlyRequired += describeRequiredPropsForRequest(
                        path.key,
                        opEntry.second.requestBody,
                        "parent path block is new in latest version"
                    )
                }
            }
        }

        return EvaluationResult(newlyRequired.isEmpty(), newlyRequired.joinToString())
    }

    /**
     * Example structure:
     *
     * ```
     * required: true
     * content:
     *   application/json:
     *     schema:
     *       type: object
     *       required:
     *         - id
     *         - name
     *       properties:
     *         id:
     *           type: integer
     *           format: int64
     *         name:
     *           type: string
     * ```
     */
    private fun describeRequiredPropsForRequest(
        path: String,
        requestBody: RequestBody?,
        reason: String
    ): List<String> {
        return requestBody?.content?.flatMap { (contentType, content) ->
            describeRequiredPropsForSchema(
                path,
                contentType,
                content.schema,
                reason
            )
        } ?: emptyList()
    }

    /**
     * Example structure:
     *
     * ```
     * type: object
     * required:
     *   - id
     *   - name
     * properties:
     *   id:
     *     type: integer
     *     format: int64
     *   name:
     *     type: string
     * ```
     */
    private fun describeRequiredPropsForSchema(
        path: String,
        contentType: String,
        schema: Schema<Any>,
        reason: String
    ): List<String> {
        // TODO don't exclude schemas that are not required, as they may have children that are
        return schema.properties
            .filter { schema.required.contains(it.key) }
            .flatMap { (propName, prop) -> describeProp(path, contentType, propName, prop, reason) }
    }

    /**
     * Example structure:
     *
     * ```
     * id:
     *   type: integer
     *   format: int64
     * ```
     *
     * or:
     *
     * ```
     * prop:
     *   type: object
     *   required:
     *     - id
     *   properties:
     *     id:
     *       type: integer
     *       format: int64
     * ```
     */
    private fun describeProp(
        path: String,
        contentType: String,
        propName: String,
        prop: Schema<Any>,
        reason: String
    ): List<String> {
        return when (prop.type) {
            // recurse
            "object" -> describeRequiredPropsForSchema(path, contentType, prop, reason)

            // iterate
            "array" -> TODO("arrays not implemented yet")

            // assume scalar
            else -> listOf(describeScalarProp(path, contentType, propName, reason))
        }
    }

    private fun describeScalarProp(path: String, contentType: String, propName: String, reason: String): String {
        return "Required property '${propName}' in $path request ($contentType): $reason"
    }

    private fun checkPathOperations(
        path: String,
        currentSpecOp: Pair<PathItem.HttpMethod, Operation>,
        previousSpecPath: PathItem
    ): MutableList<String> {
        val newlyRequired = mutableListOf<String>()

        previousSpecPath.readOperationsMap()[currentSpecOp.first]?.let { previousSpecOp ->
            // operation exists in both spec versions
            newlyRequired += checkForNewRequiredProps(path, currentSpecOp, previousSpecOp)

        } ?: run {
            // operation is new in latest version
            newlyRequired += describeRequiredPropsForRequest(
                path,
                currentSpecOp.second.requestBody,
                "parent operation block is new in latest version"
            )
        }

        return newlyRequired
    }

    private fun checkForNewRequiredProps(
        path: String,
        currentSpecOp: Pair<PathItem.HttpMethod, Operation>,
        previousSpecOp: Operation
    ): List<String> {
        val newlyRequired = mutableListOf<String>()

        // TODO check if request body 'required' property was changed
        currentSpecOp.second.requestBody?.content?.forEach { (contentType, content) ->
            previousSpecOp.requestBody?.content?.get(contentType)?.let { previousSpecContent ->
                // content for content type exists in both spec versions - check for schema differences

                content.schema.properties
                    .filter { content.schema.required.contains(it.key) }
                    .forEach { (propName, prop) ->
                        if (!previousSpecContent.schema.required.contains(propName)) {
                            newlyRequired += describeProp(
                                path = path,
                                contentType = contentType,
                                propName = propName,
                                prop = prop,
                                reason = "changed to be required in latest version"
                            )
                        }
                    }
            }
        }

        return newlyRequired
    }
}
