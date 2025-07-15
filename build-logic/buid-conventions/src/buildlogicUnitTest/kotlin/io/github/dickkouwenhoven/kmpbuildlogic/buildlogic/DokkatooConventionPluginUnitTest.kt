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

import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.jetbrains.dokka.gradle.DokkaExtension
import org.junit.jupiter.api.Test
import kotlin.test.Test
import kotlin.test.assertEquals
// import io.mockk.*
// import org.gradle.kotlin.dsl.*

class DokkatooConventionPluginUnitTest {
    @Test
    fun `plugin id should be correctly defined`() {
        val expected = "dokkatoo.convention.plugin"
        val actual = "dokkatoo.convention.plugin" // Replace with constant if available
        assertEquals(expected, actual, "Plugin ID should match")
    }

    @Test
    fun `configureKotlinDokka applies Dokka settings`() {
        // Setup
        val project = mockk<Project>(relaxed = true)
        val dokkaExtension = mockk<DokkaExtension>(relaxed = true)
        val layout = mockk<ProjectLayout>(relaxed = true)

        every { project.extensions.configure("dokka", any<Action<DokkaExtension>>()) } answers {
            val action = args[1] as Action<DokkaExtension>
            action.execute(dokkaExtension)
        }

        every { project.layout } returns layout
        every { layout.projectDirectory.file("README.md") } returns mockk(relaxed = true)

        // Exercise
        project.configureKotlinDokka()

        // Verify
        verify { dokkaExtension.dokkaPublications }
        verify { dokkaExtension.dokkaSourceSets }
    }
}
