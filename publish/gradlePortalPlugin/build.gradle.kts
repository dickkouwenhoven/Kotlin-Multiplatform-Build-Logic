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
logger.info("    ==========                Start of build.gradle.kts from publish/gradlePortalPlugin                ==========\n")
logger.info("Step 1 : applying plugins")
plugins {
    // Apply the Maven Publish plugin for generating the publication metadata for Gradle Plugins.
    // Note: from version 1.0.0 this plugin is automatically added by the 'gradle publish plugin'.
    `maven-publish`

    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins.
    // Note: from version 1.0.0 this plugin is automatically added by the 'gradle publish plugin'.
    `java-gradle-plugin`

    // Apply the shadow plugin to add automatic creation fat JARs
    alias(libs.plugins.gradleupShadowPlugin)

    // Apply the Kotlin Dsl plugin to add support for WRITING Gradle build scripts in Kotlin instead of Groovy.
    // Scope here: define plugins, tasks and configuration in Kotlin.
    `kotlin-dsl`

    // Apply the Gradle Plugin Publish plugin to make publication on Gradle Plugin Portal possible
    alias(libs.plugins.gradlePluginPublish)

    // Apply the JVM Test Suite plugin
    `jvm-test-suite`

    id("kmpbuildlogic.project.convention.plugin")
}

logger.info("Step 2 : define group name")
val kmpbuildlogicPackageName: String? = System.getProperty("kmpbuildlogic.properties.package.name")
val groupName = "$kmpbuildlogicPackageName.publish.gradle-portal-plugin"
group = groupName

logger.info("Step 3 : define version")
version = System.getProperty("kmpbuildlogic.properties.library.version")

logger.info("Step 4 : define description")
description = "This is a module that enables publishing your plugin on Gradle Portal"

logger.info("Step 5 : define task groups variables")
val kmpBuildLogicPublishGradlePortalPluginTestGroup = "KMP Build Logic Publish Gradle Portal Plugin Test"

logger.info("Step 6 : define task groups variables")
java {
    val javaVersion =
        JavaVersion.toVersion(
            System.getProperty("org.gradle.jvm.version"),
        )
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

logger.info("Step 7 : testing settings")
testing {
    suites {
        val kotlinLanguageVersion: String = System.getProperty("kotlin.language.version")

        // Shared configuration for all test suites
        @Suppress("UnstableApiUsage")
        val sharedConfig: JvmTestSuite.() -> Unit = {
            dependencies {
                implementation(project())
                implementation(libs.jetbrainsKotlinTestJunit5)
            }
            targets {
                all {
                    @Suppress("UnstableApiUsage")
                    testTask.configure {
                        useJUnitPlatform()
                        @Suppress("UnstableApiUsage")
                        useKotlinTest(kotlinLanguageVersion)
                        shouldRunAfter("test") // Prevent conflict with default 'test' task
                        group = kmpBuildLogicPublishGradlePortalPluginTestGroup
                    }
                }
            }
        }

        // Create a new test suite for Unit tests
        @Suppress("UnstableApiUsage")
        val kmpBuildLogicPublishGradlePortalPluginUnitTest =
            register<JvmTestSuite>("kmpBuildLogicPublishGradlePortalPluginUnitTest") {
                description = "Unit tests for the module publish/gradlePortalPlugin"
                sharedConfig()
            }

        // Create a new test suite for Integration tests
        @Suppress("UnstableApiUsage")
        val kmpBuildLogicPublishGradlePortalPluginIntegrationTest =
            register<JvmTestSuite>("kmpBuildLogicPublishGradlePortalPluginIntegrationTest") {
                description = "Integration tests for the module publish/gradlePortalPlugin"
                sharedConfig()
                targets {
                    all {
                        // This test suite should run after the built-in pluginUnitTest suite has run its tests
                        testTask.configure {
                            shouldRunAfter(kmpBuildLogicPublishGradlePortalPluginUnitTest)
                        }
                    }
                }
            }

        // Create a new test suite for Functional tests (e.g., TestKit)
        @Suppress("UnstableApiUsage")
        register<JvmTestSuite>("kmpBuildLogicPublishGradlePortalPluginFunctionalTest") {
            description = "Functional tests for the module publish/gradlePortalPlugin"
            sharedConfig()
            dependencies {
                implementation(project())
                implementation(gradleTestKit())
                implementation(libs.jetbrainsKotlinTest)
                implementation(libs.junitJupiterApi)
                runtimeOnly(libs.junitJupiterEngine)
            }
            targets {
                all {
                    // This test suite should run after the built-in pluginIntegrationTest suite has run its tests
                    testTask.configure {
                        shouldRunAfter(kmpBuildLogicPublishGradlePortalPluginIntegrationTest)
                    }
                }
            }
        }
    }
}

logger.info("Step 7 : making use of 'JUnitPlatform' when a task of type 'test' is used")
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

logger.info("Step 8 : adding 'kmpBuildLogicPublishGradlePortalPluginFunctionalTest' as a dependency for 'check' task")
tasks.named("check") {
    @Suppress("UnstableApiUsage")
    dependsOn(testing.suites.named("kmpBuildLogicPublishGradlePortalPluginUnitTest"))
    @Suppress("UnstableApiUsage")
    dependsOn(testing.suites.named("kmpBuildLogicPublishGradlePortalPluginFunctionalTest"))
}

logger.info("Step 9 : define the settings for shadowJar")
tasks.shadowJar {
    archiveClassifier = ""
}

logger.info("Step 10: dokkatoo settings")
dokkatoo {
    dokkatooPublications.configureEach {
        // Include the doc to be able to create a module description
        includes.from(file("docs/ModuleGradlePortalPlugin.md"))
    }

    // Creation of reference name for this module
    moduleName.set("KMP Build Logic Publish Gradle Portal Plugin")
}

logger.info("\n")
logger.info("    ==========                  End of build.gradle.kts from publish/gradlePortalPlugin                ==========\n")
