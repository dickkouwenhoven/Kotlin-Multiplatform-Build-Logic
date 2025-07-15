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
 *
 */
package io.github.dickkouwenhoven.kmpbuildlogic.publish.gradle.portal.plugin

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class KmpBuildLogicPublishGradlePortalPluginIntegrationTest {
    @TempDir
    lateinit var testProjectDir: File

    private fun createBuildFile() {
        val buildFile = File(testProjectDir, "build.gradle.kts")
        buildFile.writeText(
            """
            plugins {
                id("io.github.dickkouwenhoven.KMP-Build-Logic-Publish-Plugin")
            }

            group = "com.example"
            version = "1.0.0"
            """,
        )
    }

    @Test
    fun pluginAppliesAndExecutesSuccessfully() {
        createBuildFile()
        val runner =
            GradleRunner
                .create()
                .withProjectDir(testProjectDir)
                .withPluginClasspath()
                .withArguments("tasks")
                .forwardOutput()

        val result = runner.build()
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }
}
