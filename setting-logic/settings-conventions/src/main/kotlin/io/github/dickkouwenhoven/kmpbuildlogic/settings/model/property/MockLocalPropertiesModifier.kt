package io.github.dickkouwenhoven.kmpbuildlogic.settings.model.property

import io.github.dickkouwenhoven.kmpbuildlogic.settings.tools.LocalPropertiesModifier
import java.io.File

class MockLocalPropertiesModifier(
    private val existingLines: String = "",
    localProperties: File,
) : LocalPropertiesModifier(localProperties) {

    val putLines = mutableListOf<String>()

    override fun initiallyContains(line: String): Boolean {
        return existingLines.contains(line)
    }

    override fun putLine(line: String) {
        putLines.add(line)
    }
}