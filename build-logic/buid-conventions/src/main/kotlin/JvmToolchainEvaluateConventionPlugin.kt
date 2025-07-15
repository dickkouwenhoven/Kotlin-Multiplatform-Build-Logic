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

/**
 * Jvm Toolchain convention plugin including jvm version requirement checks.
 */

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaToolchainService

class JvmToolchainEvaluateConventionPlugin : Plugin<Project> {
    private val logger = Logging.getLogger(javaClass)

    override fun apply(project: Project) {
        with(project) {
            with(pluginManager) {
                apply("java")
            }
        }

        project.afterEvaluate {
            logLauncherJvmVersion()
            logToolchainJvmVersion(project)
        }
    }

    private fun logLauncherJvmVersion() {
        val version = System.getProperty("java.version")
        val runtime = System.getProperty("java.runtime.version")
        logger.lifecycle("🚀 Launcher JVM version : $version (runtime: $runtime)")
    }

    private fun logToolchainJvmVersion(project: Project) {
        val toolchainService = project.extensions.findByType(JavaToolchainService::class.java)
        val javaExt = project.extensions.findByType(JavaPluginExtension::class.java)

        if (toolchainService != null && javaExt != null) {
            val launcher = toolchainService.launcherFor(javaExt.toolchain).get()
            val metadata = launcher.metadata
            logger.lifecycle("⚙️ Toolchain JVM version: ${metadata.languageVersion} (${metadata.installationPath})")

            if (metadata.languageVersion.asInt() < 17) {
                throw IllegalStateException("❌ Toolchain Java version must be >= 17.")
            }
            if (metadata.languageVersion.asInt() > 24) {
                throw IllegalStateException("❌ Toolchain Java version must be <= 24.")
            }
        } else {
            logger.warn("⚠️ Toolchain service or Java extension not found.")
        }
    }
}
