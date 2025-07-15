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

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
// import io.mockk.*
// import org.gradle.kotlin.dsl.*

class KmpBuildLogicDefaultConventionPluginIntegrationTest {
    @Test
    fun `default convention plugin applies without error`() {
        val testProject = File("build/tmp/integrationTestDefaultConvention").apply { mkdirs() }
        File(testProject, "build.gradle.kts").writeText(
            """
            plugins {
                id("kmpbuildlogic.default.convention.plugin")
            }
            """.trimIndent(),
        )
        File(testProject, "settings.gradle.kts").writeText("")

        val result = GradleTestKitRunner.runBuild(testProject, "tasks")
        assertTrue(result.output.contains("BUILD SUCCESS"))
    }
}
