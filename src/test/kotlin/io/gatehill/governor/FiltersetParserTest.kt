package io.gatehill.governor

import io.gatehill.governor.filter.IgnoreObjectValueFilter
import io.gatehill.governor.filter.IgnoreParameterFilter
import io.gatehill.governor.filter.IgnorePropertyFilter
import io.gatehill.governor.support.TestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path

class FiltersetParserTest {
    private lateinit var parser: FiltersetParser
    private lateinit var filtersFile: Path

    @BeforeEach
    fun configureSystemUnderTest() {
        parser = FiltersetParser()
        filtersFile = TestUtil.findClasspathFile("/test-filters.yaml")
    }

    @Test
    fun `should parse filterset`() {
        val filterset = parser.loadFromFile(filtersFile.toString())
        assertThat(filterset.filters.size).isEqualTo(3)

        assertThat(filterset.filters[0].filter.javaClass).isEqualTo(IgnoreObjectValueFilter::class.java)
        assertThat(filterset.filters[0].config?.javaClass).isEqualTo(IgnoreObjectValueFilter.ObjectPathFilterConfig::class.java)

        assertThat(filterset.filters[1].filter.javaClass).isEqualTo(IgnoreParameterFilter::class.java)
        assertThat(filterset.filters[1].config?.javaClass).isEqualTo(IgnoreParameterFilter.IgnoreParameterConfig::class.java)

        assertThat(filterset.filters[2].filter.javaClass).isEqualTo(IgnorePropertyFilter::class.java)
        assertThat(filterset.filters[2].config?.javaClass).isEqualTo(IgnorePropertyFilter.IgnorePropertyConfig::class.java)
    }
}
