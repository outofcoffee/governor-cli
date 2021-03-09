package io.gatehill.governor.support

import java.nio.file.Paths

object TestUtil {
    fun findClasspathFile(classpathPath: String) = Paths.get(TestUtil::class.java.getResource(classpathPath).toURI())
}