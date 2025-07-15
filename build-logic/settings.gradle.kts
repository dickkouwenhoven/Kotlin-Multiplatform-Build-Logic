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

logger.info("\n")
logger.info("    ==========                       Start of settings.gradle.kts from build-logic                       ==========\n")
logger.info("Step 1 : setting of the 'repositories' within 'dependencyResolutionManagement'.")
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        google()
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
    }

    val versionProfile = System.getProperty("kmpbuildlogic.properties.version.profile") ?: "ga"
    val fileName =
        when (versionProfile) {
            "alpha" -> "../gradle/alpha.versions.toml"
            "beta" -> "../gradle/beta.versions.toml"
            "rc" -> "../gradle/rc.versions.toml"
            else -> "../gradle/libs.versions.toml"
        }
    versionCatalogs {
        create("libs") {
            from(files(fileName))
        }
    }
}

logger.info("Step 2 : 'rootProject name' setting.")
val mainRootProjectName: String? = System.getProperty("kmpbuildlogic.properties.root.project.name")
val kmpbuildlogicBuildConventionsProjectName =
    mainRootProjectName +
        "-" +
        "Build-Logic"
rootProject.name = kmpbuildlogicBuildConventionsProjectName

logger.info("Step 3 : including the module 'build-conventions'.")
include(":build-conventions")

logger.info("\n")
logger.info("    ==========                        End of settings.gradle.kts from build-logic                        ==========\n")
logger.info("    ==========                                CONFIGURATION PHASE OF LOGIC                               ==========\n")
