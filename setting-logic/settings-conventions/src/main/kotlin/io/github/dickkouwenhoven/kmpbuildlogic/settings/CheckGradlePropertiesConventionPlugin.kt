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

// Graceful configuration defaults: Filling in missing gradle.properties entries, providing warnings, and avoiding silent misconfigurations.

class CheckGradlePropertiesConventionPlugin : Plugin<Settings> {
    private val logger = Logging.getLogger(javaClass)

    override fun apply(settings: Settings) {
        val requiredProperties = mapOf(
            "org.gradle.caching" to listOf("true", "false"),
            "org.gradle.configuration-cache" to listOf("true", "false"),
            "org.gradle.buildscan.legal.terms.of.use.automatic.acceptance" to listOf("yes")
            // Add more checks with allowed values here.
        )

        requiredProperties.forEach { (key, allowedValues) ->
            val actualValue = settings.providers.gradleProperty(key).orNull
            if (actualValue != null && actualValue !in allowedValues) {
                logger.warn("[WARNING] Property '$key' has value '$actualValue', which is not among allowed values: $allowedValues")
            }
        }
    }
}
