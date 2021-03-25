package io.gatehill.governor.rules

import io.gatehill.governor.model.PropertyIdentifier
import io.gatehill.governor.model.config.ConfigMetadata
import io.gatehill.governor.model.eval.CompositeResult
import io.gatehill.governor.model.eval.EvaluationContext
import io.gatehill.governor.model.eval.EvaluationResult
import io.gatehill.governor.model.eval.PropertyResult
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.RequestBody

@ConfigMetadata("required-properties-added")
class RequiredPropertiesAddedRule : AbstractRule() {
    override fun test(context: EvaluationContext): EvaluationResult {
        context.previousSpec ?: throw IllegalStateException("No previous OpenAPI specification provided for evaluation")
        val previousSpecPaths = context.previousSpec.paths

        val newlyRequired = mutableListOf<PropertyResult>()

        context.currentSpec.paths.forEach { path ->
            newlyRequired += comparePath(previousSpecPaths, path.key, path.value)
        }

        return CompositeResult(
            success = newlyRequired.isEmpty(),
            results = newlyRequired
        )
    }

    private fun comparePath(
        previousSpecPaths: Paths,
        path: String,
        pathItem: PathItem
    ): List<PropertyResult> {
        val newlyRequired = mutableListOf<PropertyResult>()

        previousSpecPaths[path]?.let { previousSpecPath ->
            // path exists in both spec versions
            pathItem.readOperationsMap().forEach { (method, operation) ->
                newlyRequired += comparePathOperation(path, method, operation, previousSpecPath)
            }

        } ?: run {
            // path is new in latest version
            pathItem.readOperationsMap().forEach { (method, operation) ->
                newlyRequired += describeRequest(
                    path,
                    method,
                    operation.requestBody,
                    "parent path block is new in latest version"
                )
            }
        }

        return newlyRequired
    }

    private fun comparePathOperation(
        path: String,
        currentSpecMethod: PathItem.HttpMethod,
        currentSpecOp: Operation,
        previousSpecPath: PathItem
    ): List<PropertyResult> {
        val newlyRequired = mutableListOf<PropertyResult>()

        previousSpecPath.readOperationsMap()[currentSpecMethod]?.let { previousSpecOp ->
            // operation exists in both spec versions
            newlyRequired += compareRequest(
                path = path,
                method = currentSpecMethod,
                currentSpecOp = currentSpecOp,
                previousSpecOp = previousSpecOp
            )

            // TODO check for *removals* of required properties in response schema

        } ?: run {
            // operation is new in latest version
            newlyRequired += describeRequest(
                path = path,
                method = currentSpecMethod,
                requestBody = currentSpecOp.requestBody,
                reason = "parent operation block is new in latest version"
            )
        }

        return newlyRequired
    }

    private fun compareRequest(
        path: String,
        method: PathItem.HttpMethod,
        currentSpecOp: Operation,
        previousSpecOp: Operation
    ): List<PropertyResult> {
        val newlyRequired = mutableListOf<PropertyResult>()

        currentSpecOp.requestBody?.content?.forEach { (contentType, content) ->
            newlyRequired += compareRequestContent(
                path = path,
                method = method,
                currentSpecOp = currentSpecOp,
                previousSpecOp = previousSpecOp,
                contentType = contentType,
                content = content
            )
        }

        return newlyRequired
    }

    private fun compareRequestContent(
        path: String,
        method: PathItem.HttpMethod,
        currentSpecOp: Operation,
        previousSpecOp: Operation,
        contentType: String,
        content: MediaType
    ): List<PropertyResult> {
        val newlyRequired = mutableListOf<PropertyResult>()

        // TODO check request body 'required' property (and if it has changed)

        val previousSpecContent = previousSpecOp.requestBody?.content?.get(contentType)
        if (null != previousSpecContent) {
            // content for content type exists in both spec versions - check for schema differences
            newlyRequired += compareRequestSchemas(path, method, contentType, content, previousSpecContent)

        } else {
            // requestBody is new in latest version (but operation previously existed)
            newlyRequired += describeRequest(
                path = path,
                method = method,
                requestBody = currentSpecOp.requestBody,
                reason = "request body is new in latest version"
            )
        }

        return newlyRequired
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
    private fun describeRequest(
        path: String,
        method: PathItem.HttpMethod,
        requestBody: RequestBody?,
        reason: String
    ): List<PropertyResult> {
        return requestBody?.content?.flatMap { (contentType, content) ->
            describeSchema(
                path = path,
                method = method,
                contentType = contentType,
                propNameOrDescription = "requestBody",
                schema = content.schema,
                reason = reason
            )
        } ?: emptyList()
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
     * or object:
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
     *
     * or array of items:
     *
     * ```
     * prop:
     *   type: array
     *   items:
     *     type: object
     *     required:
     *       - id
     *     properties:
     *       id:
     *         type: integer
     *         format: int64
     * ```
     */
    private fun describeSchema(
        path: String,
        method: PathItem.HttpMethod,
        contentType: String,
        propNameOrDescription: String,
        schema: Schema<*>,
        reason: String
    ): List<PropertyResult> {
        return when (schema.type) {
            // recurse
            "object" -> describeSchemaRequiredProps(path, method, contentType, schema, reason)

            // iterate
            "array" -> {
                val arraySchema = schema as ArraySchema

                // TODO support mixed type arrays (oneOf/anyOf etc.)

                describeSchemaRequiredProps(path, method, contentType, arraySchema.items, reason)
            }

            // assume scalar, non-object type, like string, number etc.
            else -> listOf(describeScalarProp(path, method, contentType, propNameOrDescription, reason))
        }
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
    private fun describeSchemaRequiredProps(
        path: String,
        method: PathItem.HttpMethod,
        contentType: String,
        schema: Schema<*>,
        reason: String
    ): List<PropertyResult> {
        // include properties that are not marked 'required' if they have children (that might be required)
        return schema.properties
            ?.filter { it.value.type == "object" || schema.required.contains(it.key) }
            ?.flatMap { (propName, prop) -> describeSchema(path, method, contentType, propName, prop, reason) }
            ?: emptyList()
    }

    private fun compareRequestSchemas(
        path: String,
        method: PathItem.HttpMethod,
        contentType: String,
        currentContent: MediaType,
        previousContent: MediaType
    ): List<PropertyResult> {
        val newlyRequired = mutableListOf<PropertyResult>()

        // check schema types are the same
        if (currentContent.schema?.type != previousContent.schema?.type) {
            // schema type is different - this is a breaking change
            newlyRequired += PropertyResult(
                success = false,
                message = "Request schema type in $path request ($contentType) changed from ${currentContent.schema?.type} to ${previousContent.schema?.type} in latest version",
                PropertyIdentifier(path, contentType, "requestBody")
            )
            return newlyRequired
        }

        if (currentContent.schema?.type == "array") {
            // compare array schemas to see if items have changed
            newlyRequired += compareArraySchemas(
                path,
                method,
                contentType,
                currentContent.schema as ArraySchema,
                previousContent.schema as ArraySchema
            )

        } else {
            // compare object schema required properties
            newlyRequired += compareObjectSchemas(
                path,
                method,
                contentType,
                currentContent.schema,
                previousContent.schema
            )
        }

        return newlyRequired
    }

    private fun compareObjectSchemas(
        path: String,
        method: PathItem.HttpMethod,
        contentType: String,
        currentSchema: Schema<*>,
        previousSchema: Schema<*>
    ): List<PropertyResult> {
        val newlyRequired = mutableListOf<PropertyResult>()

        val latestRequiredProps = currentSchema.properties?.filter {
            currentSchema.required?.contains(it.key) == true
        }

        latestRequiredProps?.forEach { (propName, prop) ->
            if (!previousSchema.required.contains(propName)) {
                newlyRequired += describeSchema(
                    path = path,
                    method = method,
                    contentType = contentType,
                    propNameOrDescription = propName,
                    schema = prop,
                    reason = "newly required in latest version"
                )
            }
        }

        return newlyRequired
    }

    private fun compareArraySchemas(
        path: String,
        method: PathItem.HttpMethod,
        contentType: String,
        currentArray: ArraySchema,
        previousArray: ArraySchema
    ): List<PropertyResult> {
        return compareObjectSchemas(
            path,
            method,
            contentType,
            currentArray.items,
            previousArray.items
        )
    }

    private fun describeScalarProp(
        path: String,
        method: PathItem.HttpMethod,
        contentType: String,
        propName: String,
        reason: String
    ) = PropertyResult(
        success = false,
        "Required property '${propName}' in $method $path request ($contentType): $reason",
        property = PropertyIdentifier(path, contentType, propName)
    )
}
