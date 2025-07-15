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
import org.jetbrains.dokka.DokkaDefaults.moduleName

logger.info("\n")
logger.info("    ==========                    Start of build.gradle.kts from module annotations                    ==========\n")
logger.info("Step 1 : applying plugins")
plugins {
    // Apply the Java Library plugin to provide functions which can be used for the creation of annotations
    `java-library`

    // Apply the Kotlin Dsl plugin to add support for Kotlin.
    `kotlin-dsl`

    // Apply the shadow plugin to add automatic creation fat JARs
    alias(libs.plugins.gradleupShadowPlugin)

    id("kmpbuildlogic.project.convention.plugin")
}

logger.info("Step 2 : define group name")
val kmpbuildlogicPackageName: String? = System.getProperty("kmpbuildlogic.properties.package.name")
logger.debug("Group name is: $kmpbuildlogicPackageName")
group = kmpbuildlogicPackageName.toString()

logger.info("Step 3 : define version")
val kmpbuildlogicLibraryVersion: String? = System.getProperty("kmpbuildlogic.properties.library.version")
logger.debug("Library Version is: $kmpbuildlogicLibraryVersion")
version = kmpbuildlogicLibraryVersion.toString()

logger.info("Step 4 : define description")
description = "This is a module that defines annotations which are used in this project"

logger.info("Step 5 : Java settings added")
java {
    val javaVersion =
        JavaVersion.toVersion(
            System.getProperty("org.gradle.jvm.version"),
        )
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    withJavadocJar()
    withSourcesJar()
}

logger.info("Step 6 : define the settings for shadowJar")
tasks.shadowJar {
    archiveClassifier = ""
}

logger.info("Step 7: dokkatoo settings")
dokkatoo {
    dokkatooPublications.configureEach {
        // Include the doc to be able to create a module description
        includes.from(file("docs/ModuleAnnotations.md"))
    }

    // Creation of reference name for this module
    moduleName.set("KMP Build Logic Annotations")
}

logger.info("\n")
logger.info("    ==========                     End of build.gradle.kts from module annotations                     ==========\n")
