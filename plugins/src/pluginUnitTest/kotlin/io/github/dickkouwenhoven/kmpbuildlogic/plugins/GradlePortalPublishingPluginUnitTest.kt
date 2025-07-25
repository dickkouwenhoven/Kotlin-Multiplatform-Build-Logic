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
package io.github.dickkouwenhoven.kmpbuildlogic.plugins

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test

class GradlePortalPublishingPluginUnitTest {
    @Test
    fun doAUnitTest_pluginAppliesCorrectly() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GradlePortalPublishingPlugin::class.java)
        check(project.plugins.hasPlugin("io.github.dickkouwenhoven.kmpbuildlogic.gradlePortalPublishingPlugin"))
    }

    @Test
    fun doAUnitTest_extensionIsCreated() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GradlePortalPublishingPlugin::class.java)
        val extension = project.extensions.findByName("gradlePortalPublishing")
        checkNotNull(extension)
    }
}
