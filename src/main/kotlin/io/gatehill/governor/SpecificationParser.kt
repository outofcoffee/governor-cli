package io.gatehill.governor

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import java.nio.file.Paths

class SpecificationParser {
    fun loadFromFile(specFilePath: String): OpenAPI {
        val specFile = Paths.get(specFilePath).toFile()
        if (!specFile.exists()) {
            throw IllegalArgumentException("OpenAPI specification file: ${specFilePath} does not exist")
        }

        return OpenAPIV3Parser().read(specFilePath)
    }

    companion object {
        val defaultInstance = SpecificationParser()
    }
}
