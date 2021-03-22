package io.gatehill.governor

import com.fasterxml.jackson.module.kotlin.readValue
import io.gatehill.governor.filter.IgnoreObjectPathFilter
import io.gatehill.governor.filter.IgnoreParameterFilter
import io.gatehill.governor.filter.IgnorePropertyFilter
import io.gatehill.governor.model.config.FiltersetDef
import io.gatehill.governor.model.filter.Filter
import io.gatehill.governor.model.filter.Filterset
import io.gatehill.governor.model.filter.ReifiedFilter
import io.gatehill.governor.util.SerialisationUtil
import java.nio.file.Paths

class FiltersetParser {
    private val registeredFilters = listOf<Filter>(
        IgnoreObjectPathFilter(),
        IgnoreParameterFilter(),
        IgnorePropertyFilter()
    )

    private val filters = registeredFilters.map { filter -> return@map filter.info.name to filter }.toMap()

    @Suppress("UNCHECKED_CAST")
    fun loadFromFile(filtersFilePath: String): Filterset {
        val filtersFile = Paths.get(filtersFilePath).toFile()
        if (!filtersFile.exists()) {
            throw IllegalArgumentException("Filters file: $filtersFilePath does not exist")
        }

        val filtersetDef = SerialisationUtil.yamlMapper.readValue<FiltersetDef>(filtersFile)

        return Filterset(filtersetDef.filters.map { filterDef ->
            if (filterDef is String) {
                ReifiedFilter(
                    filter = lookupFilter(filterDef)
                )
            } else {
                val filterWithConfig = filterDef as Map<String, Map<String, Any>>
                val filterName = filterWithConfig.keys.first()
                val filter = lookupFilter(filterName)
                val configMap = filterWithConfig[filterName]
                ReifiedFilter(
                    filter = filter,
                    config = SerialisationUtil.yamlMapper.convertValue(configMap, filter.configClass)
                )
            }
        })
    }

    private fun lookupFilter(filterName: String) =
        filters[filterName] ?: throw IllegalStateException("No matching filter with name: $filterName")

    companion object {
        val defaultInstance = FiltersetParser()
    }
}
