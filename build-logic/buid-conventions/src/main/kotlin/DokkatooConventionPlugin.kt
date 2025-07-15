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
 * Common conventions for generating documentation with Dokkatoo.
 */

import dev.adamko.dokkatoo.DokkatooExtension
import dev.adamko.dokkatoo.dokka.plugins.DokkaHtmlPluginParameters
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named

class DokkatooConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            with(pluginManager) {
                apply("dev.adamko.dokkatoo-html")
            }
            extensions.configure<DokkatooExtension> {
                dokkatooPublicationDirectory.set(layout.buildDirectory.dir("dokkatoo"))

                dokkatooSourceSets.configureEach {
                    sourceLink {
                        val githubUrl = System.getProperty("kmpbuildlogic.properties.github.url")
                        remoteUrl(githubUrl)
                        localDirectory.set(rootDir)
                    }
                    suppress.set(false)
                    enableJdkDocumentationLink.set(true)
                    enableKotlinStdLibDocumentationLink.set(true)
                }

                pluginsConfiguration.named<DokkaHtmlPluginParameters>("html") {
                    customAssets.from(
                        rootProject.file("documentation/assets/kmp-build-logic-logo.png"),
                        rootProject.file("documentation/assets/kmp-build-logic-logo.webp"),
                        rootProject.file("documentation/assets/logo-icon.svg"),
                    )
                    customStyleSheets.from(rootProject.file("documentation/styles/kmp-build-logic-styles.css"))
                    templatesDir.set(rootProject.file("documentation/templates"))
                }
            }
        }
    }
}
