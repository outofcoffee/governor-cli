package io.gatehill.governor

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths

class SpecificationParserTest {
    private lateinit var parser: SpecificationParser
    private lateinit var rulesFile: Path

    @BeforeEach
    fun configureSystemUnderTest() {
        parser = SpecificationParser()
        rulesFile = Paths.get(SpecificationParserTest::class.java.getResource("/test-spec.yaml").toURI())
    }

    @Test
    @DisplayName("Should parse specification")
    fun shouldParseSpec() {
        val spec = parser.loadFromFile(rulesFile.toString())
        assertThat(spec.info.title).isEqualTo("Swagger Petstore")
    }
}
