package io.gatehill.governor.model

open class PropertyIdentifier(
    val path: String,
    val contentType: String,
    val property: String
) {
    override fun toString()= "PropertyIdentifier(path='$path', contentType='$contentType', property='$property')"

    /**
     * Performs an equality check, where `contentType` is case insensitive, and all other properties are case sensitive.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PropertyIdentifier) return false

        if (path != other.path) return false
        if (!contentType.equals(other.contentType, ignoreCase = true)) return false
        if (property != other.property) return false

        return true
    }

    /**
     * Performs an equality check, where `contentType` is case insensitive, and all other properties are case sensitive.
     */
    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + contentType.toLowerCase().hashCode()
        result = 31 * result + property.hashCode()
        return result
    }
}