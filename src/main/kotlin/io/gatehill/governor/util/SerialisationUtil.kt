package io.gatehill.governor.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object SerialisationUtil {
    val jsonMapper = ObjectMapper().customiseMapper()
    val yamlMapper = YAMLMapper().customiseMapper()

    private fun ObjectMapper.customiseMapper(): ObjectMapper =
        this.registerKotlinModule().registerModule(JavaTimeModule())
}
