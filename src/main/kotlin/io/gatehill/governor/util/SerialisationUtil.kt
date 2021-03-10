package io.gatehill.governor.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object SerialisationUtil {
    val jsonMapper = ObjectMapper().registerKotlinModule()
    val yamlMapper = YAMLMapper().registerKotlinModule()
}