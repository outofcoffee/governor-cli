package io.gatehill.governor.rules

import io.gatehill.governor.model.config.ConfigMetadata
import io.gatehill.governor.model.ParameterIdentifier
import io.gatehill.governor.model.eval.*
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.parameters.Parameter

@ConfigMetadata("required-parameters-added")
class RequiredParametersAddedRule : AbstractRule() {
    override fun test(context: EvaluationContext): EvaluationResult {
        context.previousSpec ?: throw IllegalStateException("No previous OpenAPI specification provided for evaluation")
        val previousSpecPaths = context.previousSpec.paths

        val newlyRequired = mutableListOf<ParameterResult>()

        context.currentSpec.paths.forEach { path ->
            previousSpecPaths[path.key]?.let { previousSpecPath ->
                // path exists in both spec versions
                path.value.readOperationsMap().map { it.toPair() }.forEach { opEntry ->
                    newlyRequired += checkPathOperations(path.key, opEntry, previousSpecPath)
                }

            } ?: run {
                // path is new in latest version
                path.value.readOperationsMap().map { it.toPair() }.forEach { opEntry ->
                    newlyRequired += describeRequiredParams(
                        path.key,
                        opEntry,
                        "parent path block is new in latest version"
                    )
                }
            }
        }

        return CompositeResult(
            success = newlyRequired.isEmpty(),
            results = newlyRequired
        )
    }

    private fun describeRequiredParams(
        path: String,
        opEntry: Pair<PathItem.HttpMethod, Operation>,
        reason: String
    ) = opEntry.second.parameters?.filter { it.required }?.map { describeRequiredParam(path, opEntry, it, reason) }
        ?: emptyList()

    private fun describeRequiredParam(
        path: String,
        opEntry: Pair<PathItem.HttpMethod, Operation>,
        parameter: Parameter,
        reason: String
    ) = ParameterResult(
        success = false,
        message = "Required parameter '${parameter.name}' in ${opEntry.first.name} $path: $reason",
        ParameterIdentifier(path, opEntry.first.name, parameter.name)
    )

    private fun checkPathOperations(
        path: String,
        currentSpecOp: Pair<PathItem.HttpMethod, Operation>,
        previousSpecPath: PathItem
    ): List<ParameterResult> {
        val newlyRequired = mutableListOf<ParameterResult>()

        previousSpecPath.readOperationsMap()[currentSpecOp.first]?.let { previousSpecOp ->
            // operation exists in both spec versions
            newlyRequired += checkForNewRequiredParams(path, currentSpecOp, previousSpecOp)

        } ?: run {
            // operation is new in latest version
            newlyRequired += describeRequiredParams(
                path,
                currentSpecOp,
                "parent operation block is new in latest version"
            )
        }

        return newlyRequired
    }

    private fun checkForNewRequiredParams(
        path: String,
        currentSpecOp: Pair<PathItem.HttpMethod, Operation>,
        previousSpecOp: Operation
    ): List<ParameterResult> {
        val newlyRequired = mutableListOf<ParameterResult>()

        currentSpecOp.second.parameters?.filter { it.required }?.forEach { parameter ->
            previousSpecOp.parameters?.find { it.name == parameter.name }?.let { previousSpecParam ->
                // parameter exists in both spec versions - check if changed to be required
                if (!previousSpecParam.required) {
                    newlyRequired += describeRequiredParam(
                        path,
                        currentSpecOp,
                        parameter,
                        "changed to be required in latest version"
                    )
                }

            } ?: run {
                // parameter is new in latest version
                newlyRequired += describeRequiredParam(path, currentSpecOp, parameter, "new in latest version")
            }
        }

        return newlyRequired
    }
}
