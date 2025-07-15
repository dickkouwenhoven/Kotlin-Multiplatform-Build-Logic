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
package io.github.dickkouwenhoven.kmpbuildlogic.publish

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText

class GradlePortalPublishingPluginFunctionalTest {
    @Test
    fun doAPluginAppliesSuccessfullyTest() {
        val projectDir = createTempDirectory()
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm") version "2.0.21"
                id("io.github.dickkouwenhoven.kmpbuildlogic.gradlePortalPublishingPlugin")
            }

            repositories {
                gradlePluginPortal()
                mavenCentral()
            }
            """,
        )

        val result =
            GradleRunner
                .create()
                .withProjectDir(projectDir.toFile())
                .withArguments("tasks")
                .withPluginClasspath()
                .build()

        assertTrue(result.output.contains("gradlePortalPublishing"))
    }
}
