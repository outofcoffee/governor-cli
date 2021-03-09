package io.gatehill.governor

import io.gatehill.governor.support.TestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Path

class SpecificationParserTest {
    private lateinit var parser: SpecificationParser
    private lateinit var specFile: Path

    @BeforeEach
    fun configureSystemUnderTest() {
        parser = SpecificationParser()
        specFile = TestUtil.findClasspathFile("/test-spec.yaml")
    }

    @Test
    @DisplayName("Should parse specification")
    fun `should parse OpenAPI spec`() {
        val spec = parser.loadFromFile(specFile.toString())
        assertThat(spec.info.title).isEqualTo("Swagger Petstore")
    }
}
