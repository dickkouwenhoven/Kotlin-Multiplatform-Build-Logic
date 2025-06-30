/*
 * Copyright 2022 Dick Kouwenhoven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.dickkouwenhoven.kmpbuildlogic.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging
import java.io.File

// Version and dependency alignment: Preventing unexpected behavior when users have different versions of libraries or omit libraries that your plugin relies on.

class CheckVersionRequirementsConventionPlugin : Plugin<Settings> {
    private val logger = Logging.getLogger(javaClass)

    override fun apply(settings: Settings) {
        val versionFile = File(settings.rootDir, "requirements.versions.toml")
        if (!versionFile.exists()) {
            logger.warn("[WARNING] requirements.versions.toml not found in root project.")
            return
        }

        val tomlText = versionFile.readText()
        val versionsToCheck = mapOf(
            "gradle" to settings.gradle.gradleVersion,
            "kotlin" to KotlinVersion.CURRENT,
            "jvm" to System.getProperty("java.version")
            // Add other tools as needed
        )

        versionsToCheck.forEach { (name, currentVersion) ->
            val requiredRegex = Regex("$name\\s*=\\s*\\{[^}]*require\\s*=\\s*\"([^\"]+)\"")
            val match = requiredRegex.find(tomlText)
            if (match != null) {
                val requiredRange = match.groupValues[1]
                if (!versionInRange(currentVersion as String, requiredRange)) {
                    logger.warn("[WARNING] Version for '$name' ($currentVersion) is not in required range $requiredRange")
                }
            }
        }
    }

    private fun versionInRange(version: String, range: String): Boolean {
        // Basic inclusive range check (e.g. [1.0,2.0))
        val match = Regex("\\[(\\d+(?:\\.\\d+)*),\\s*(\\d+(?:\\.\\d+)*)\\)").matchEntire(range)
            ?: return true // fallback if range is malformed
        val (min, max) = match.destructured
        return version >= min && version < max
    }
}
