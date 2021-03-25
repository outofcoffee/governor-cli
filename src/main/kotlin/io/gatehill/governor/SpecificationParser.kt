package io.gatehill.governor

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import java.nio.file.Paths

class SpecificationParser {
    fun loadFromFile(specFilePath: String): OpenAPI {
        val specFile = Paths.get(specFilePath).toFile()
        if (!specFile.exists()) {
            throw IllegalArgumentException("OpenAPI specification file: $specFile does not exist")
        }

        // resolve references fully to enable property/schema traversal
        return OpenAPIV3Parser().read(specFilePath, null, ParseOptions().apply { isResolveFully = true })
    }

    companion object {
        val defaultInstance = SpecificationParser()
    }
}
