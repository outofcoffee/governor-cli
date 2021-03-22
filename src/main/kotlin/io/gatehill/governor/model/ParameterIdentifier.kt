package io.gatehill.governor.model

open class ParameterIdentifier(
    val path: String,
    val operation: String,
    val parameter: String
) {
    override fun toString() = "ParameterIdentifier(path='$path', operation='$operation', parameter='$parameter')"

    /**
     * Performs an equality check, where `operation` is case insensitive, and all other properties are case sensitive.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParameterIdentifier) return false

        if (path != other.path) return false
        if (!operation.equals(other.operation, ignoreCase = true)) return false
        if (parameter != other.parameter) return false

        return true
    }

    /**
     * Performs an equality check, where `operation` is case insensitive, and all other properties are case sensitive.
     */
    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + operation.toLowerCase().hashCode()
        result = 31 * result + parameter.hashCode()
        return result
    }
}