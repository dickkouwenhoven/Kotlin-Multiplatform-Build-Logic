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
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.PluginAware
import org.gradle.api.logging.Logging
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.CompileUsingKotlinDaemon

class DaemonConfigConventionPlugin: Plugin<PluginAware> {
    private val logger = Logging.getLogger(javaClass)

    override fun apply(target: PluginAware) {
        when (target) {
            is Gradle -> {
                // This plugin should not run before Settings becomes active
                logger.warn("DaemonConfigConventionPlugin should be applied within Settings, not within Gradle.")
                return
            }
            is Settings -> {
                target.gradle.beforeProject {
                    plugins.withType<KotlinBasePlugin>{
                        tasks.withType<CompileUsingKotlinDaemon>().configureEach {
                            val jvmCountry      = project.providers.gradleProperty("org.gradle.jvmargs.country").orNull
                            val jvmEncoding     = project.providers.gradleProperty("org.gradle.jvmargs.encoding").orNull
                            val jvmLanguage     = project.providers.gradleProperty("org.gradle.jvmargs.language").orNull
                            val jvmMaxMetaspace = project.providers.gradleProperty("org.gradle.jvmargs.max.metaspace.size").orNull
                            val jvmMemory       = project.providers.gradleProperty("org.gradle.jvmargs.memory").orNull
                            val jvmOutOfMemory  = project.providers.gradleProperty("org.gradle.jvmargs.out.of.memory").orNull
                            val jvmPreTouch     = project.providers.gradleProperty("org.gradle.jvmargs.pre.touch").orNull
                            val jvmParallelGc   = project.providers.gradleProperty("org.gradle.jvmargs.parallel.gc").orNull
                            kotlinDaemonJvmArguments.addAll(
                                listOfNotNull(
                                    jvmCountry,
                                    jvmEncoding,
                                    jvmLanguage,
                                    jvmMaxMetaspace,
                                    jvmMemory,
                                    jvmOutOfMemory,
                                    jvmPreTouch,
                                    jvmParallelGc
                                )
                            )
                        }
                    }
                    logger.lifecycle("✅ Daemon configuration settings activated successfully.")
                }
            }
            is Project -> {
                // This plugin should run within Settings, not within Project
                logger.warn("DaemonConfigConventionPlugin should be applied within Settings, not within Project.")
                return
            }
            else -> {
                logger.warn("Unknow pluginAware type! PluginAware found is: $target.")
                return
            }
        }
    }
}
