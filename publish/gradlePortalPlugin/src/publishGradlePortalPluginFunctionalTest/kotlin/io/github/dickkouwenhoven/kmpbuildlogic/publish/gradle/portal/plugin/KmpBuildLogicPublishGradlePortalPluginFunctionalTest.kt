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
package io.github.dickkouwenhoven.publish.gradle.portal.plugin

import org.junit.jupiter.api.io.TempDir
import java.io.File

class KmpBuildLogicPublishGradlePortalPluginFunctionalTest {
    @TempDir
    lateinit var testProjectDir: File

    private fun createBuildFile() {
        val buildFile = File(testProjectDir, "build.gradle.kts")
        buildFile.writeText(
            """
            plugins {
                id("io.github.dickkouwenhoven.KMP-Build-Logic-Publish-Plugin")
            }

            group = "io.github.dickkouwenhoven"
            version = "1.0.0"

            repositories {
                mavenCentral()
            }
            """,
        )
    }

    @Test
    fun publishingToLocalMavenRepositorySucceeds() {
        createBuildFile()
        val runner =
            GradleRunner
                .create()
                .withProjectDir(testProjectDir)
                .withPluginClasspath()
                .withArguments("publishToMavenLocal")
                .forwardOutput()

        val result = runner.build()
        assertTrue(result.output.contains("BUILD SUCCESSFUL"))
    }
}
