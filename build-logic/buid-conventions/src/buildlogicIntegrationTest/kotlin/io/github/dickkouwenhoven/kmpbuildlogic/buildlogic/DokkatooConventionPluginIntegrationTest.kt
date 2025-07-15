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
package io.github.dickkouwenhoven.kmpbuildlogic.buildlogic

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class DokkatooConventionPluginIntegrationTest {
    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun `plugin applies dokka and configures html output`() {
        // Arrange
        val buildFile = File(testProjectDir, "build.gradle.kts")
        buildFile.writeText(
            """
            plugins {
                id("io.github.dickkouwenhoven.kmpbuildlogic.kotlindokka")
            }

            System.setProperty("kmpbuildlogic.properties.github.url", "https://github.com/dummy/repo")
            """.trimIndent(),
        )

        File(testProjectDir, "README.md").writeText("# Example README")

        // Act
        val result =
            GradleRunner
                .create()
                .withProjectDir(testProjectDir)
                .withPluginClasspath()
                .withArguments("dokkaHtml")
                .build()

        // Assert
        assertTrue(result.output.contains("Task :dokkaHtml"))
        assertTrue(File(testProjectDir, "docs/api/0.x").exists())
    }

    @Test
    fun `project applies dokkatoo plugin via convention plugin`() {
        val projectDir = File("build/tmp/integrationTestProject").apply { mkdirs() }
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("dokkatoo.convention.plugin")
            }
            """.trimIndent(),
        )
        File(projectDir, "settings.gradle.kts").writeText("")

        val result = GradleTestKitRunner.runBuild(projectDir, "tasks")
        assertTrue("dokkatoo" in result.output, "Expected dokkatoo task to be present")
    }
}
