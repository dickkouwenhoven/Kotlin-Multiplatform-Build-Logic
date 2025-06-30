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

plugins {
    kotlin("jvm")
    alias(libs.plugins.kmpBuildLogicProjectPlugin)
}

dependencies {
    dokkatooPluginHtml(libs.jetbrainsDokkaAllModulesPagePlugin)
    dokkatooPluginHtml(libs.jetbrainsDokkaVersioningPlugin)

    dokkatoo(project(":childProjectA"))
    dokkatoo(project(":childProjectB"))
}

val currentVersion = "1.0.0"
val previousVersionsDirectory: Directory = layout.projectDirectory.dir("previousDocVersions")

dokkatoo {
    dokkatooPublications.configureEach {
        includes.from("DocsModule.md")
    }
    moduleName.set("KMP Build Logic")
    pluginsConfiguration {
        // Main configuration for the versioning plugin:
        versioning {
            // Generate documentation for the current version of the application.
            version = currentVersion

            // Look for previous versions of docs in the directory defined in
            // `previousVersionsDirectory` allowing it to create the version
            // navigation dropdown menu.
            olderVersionsDir = previousVersionsDirectory
        }
    }
    pluginsConfiguration.html {
        templatesDir.set(rootProject.file("documentation/templates"))
        customStyleSheets.from(rootProject.file("documentation/styles/kmp-build-logic-styles.css"))
        customAssets.from(
            rootProject.file("documentation/assets/kmp-build-logic-logo.png"),
            rootProject.file("documentation/assets/kmp-build-logic-logo.webp"),
            rootProject.file("documentation/assets/logo-icon.svg"),
        )
    }
}

