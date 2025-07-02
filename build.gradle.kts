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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

logger.info("\n")
logger.info("    ==========                        Start of build.gradle.kts from module root                       ==========")

logger.info("Step 1 : creation of variable 'ktlint'")
val ktlint: Configuration by configurations.creating

logger.info("Step 2 : buildscript")
buildscript {
    /**
     * Global Security Fixes for Common Dependencies
     *
     * Enforces minimum secure versions for commonly used libraries across all subprojects.
     * These overrides address known vulnerabilities in transitive dependencies that might
     * be pulled in by various subprojects.
     *
     * Affected Libraries:
     * └── org.apache.commons
     *     ├── commons-compress:* → 1.27.1
     *     └── commons-io:* → 2.18.0
     *
     * Affected Libraries:
     * ├── org.apache.commons
     * ├   ├── commons-compress:* → 1.27.1
     * ├   └── commons-io:* → 2.18.0
     * ├
     * ├── com.fasterxml.jackson.core
     * ├   └── jackson-core:* → 2.15.0-rc1
     * ├
     * └── com.fasterxml.woodstox
     *     └── woodstox-core:* → 6.40
     *
     * Mitigated Vulnerabilities:
     * 1. Commons Compress
     *    - CVE-2024-26308: Potential security vulnerability solved
     *    - CVE-2024-25710: Input validation weakness solved
     *    - CVE-2023-42503: Potential code execution risk solved
     *
     * 2. Commons IO
     *    - CVE-2024-26308: Security vulnerability
     *    - CVE-2023-42503: Input processing risk
     *
     * 3. Jackson Core
     *    - WS-2022-0468: Denial of Service (DoS) vulnerability
     *
     * 4. Woodstox Core
     *    - CVE-2022-40152: Denial of Service (DoS) vulnerability
     *
     */
    configurations.all {
        resolutionStrategy.eachDependency {
            val apacheCommonsCompress = providers.gradleProperty("apache.commons.compress.version").toString()
            val apacheCommonsIo = providers.gradleProperty("apache.commons.io.version").toString()

            // Apache Commons libraries
            if (requested.group == "org.apache.commons" && requested.name == "commons-compress") {
                useVersion(apacheCommonsCompress)
                because("CVE-2024-26308, CVE-2024-25710, CVE-2023-42503")
            }
            if (requested.group == "commons-io" && requested.name == "commons-io") {
                useVersion(apacheCommonsIo)
                because("CVE-2024-26308, CVE-2023-42503")
            }

            val fasterxmlJacksonCore = providers.gradleProperty("fasterxml.jackson.core.version").toString()
            val fasterxmlWoodstoxCore = providers.gradleProperty("fasterxml.woodstox.core.version").toString()

            // FasterXML libraries
            if (requested.group == "com.fasterxml.jackson.core" && requested.name == "jackson-core") {
                useVersion(fasterxmlJacksonCore)
                because("WS-2022-0488")
            }
            if (requested.group == "com.fasterxml.woodstox" && requested.name == "woodstox-core") {
                useVersion(fasterxmlWoodstoxCore)
                because("CVE-2022-40152")
            }
        }
    }
}

logger.info("Step 3 : adding project plugins")
plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`

    // Apply the Kotlin Dsl plugin to add support for Kotlin.
    `kotlin-dsl`
}

logger.info("Step 4 : registering of tasks")
tasks.register("printFullyQualifiedTasks") {
    group = "help"
    description = "Prints all tasks with their fully qualified path"
    doLast {
        allprojects.forEach { project ->
            project.tasks.forEach { task ->
                println("${project.path}:${task.name}")
            }
        }
    }
}

logger.info("\n")
logger.info("    ==========                                  EXECUTION PHASE OF LOGIC                                 ==========\n")
