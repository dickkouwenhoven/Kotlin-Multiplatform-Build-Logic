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
logger.info("    ==========            Start of build.gradle.kts from setting-logic/settings-conventions            ==========\n")

logger.info("Step 1 : applying plugins")
plugins {
    // Apply the Jetbrains Kotlin Jvm plugin to add support for COMPILING Kotlin code for JVM.
    alias(libs.plugins.jetbrainsKotlinJvm)

    // Apply the Java Library plugin to provide functions which can be used for annotations
    `java-library`

    // Apply the JVM Test Suite plugin
    `jvm-test-suite`

    // Apply the Kotlin Dsl plugin to add support for Kotlin.
    `kotlin-dsl`

    // Apply the Kotlin serialization plugin to add support for reading/writing from/to a file
    alias(libs.plugins.jetbrainsKotlinPluginSerialization)
}

logger.info("Step 2 : define group name")
val kmpBuildLogicPackageName: String? = System.getProperty("kmpbuildlogic.properties.package.name")
val groupName = "$kmpBuildLogicPackageName.setting-logic"
group = groupName

logger.info("Step 3 : define version")
version = System.getProperty("kmpbuildlogic.properties.library.version")

logger.info("Step 4 : define description")
description = "This is a standard module to enable created convention plugins to be used within settings files"

logger.info("Step 5 : define task groups variables")
val kmpBuildLogicSettingLogicTestGroup = "KMP Build Logic Setting Logic Test"

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
                    testTask.configure {
                        useJUnitPlatform()
                        useKotlinTest(kotlinLanguageVersion)
                        shouldRunAfter("test") // Prevent conflict with default 'test' task
                        group = kmpBuildLogicSettingLogicTestGroup
                    }
                }
            }
        }

        // Create a new test suite for Unit tests
        @Suppress("UnstableApiUsage")
        val settingLogicUnitTest =
            register<JvmTestSuite>("kmpBuildLogicSettingLogicUnitTest") {
                description = "Unit tests for the module setting-logic"
                sharedConfig()
            }

        // Create a new test suite for Integration tests
        @Suppress("UnstableApiUsage")
        val settingLogicIntegrationTest =
            register<JvmTestSuite>("kmpBuildLogicSettingLogicIntegrationTest") {
                description = "Integration tests for the module setting-logic"
                sharedConfig()
                targets {
                    all {
                        // This test suite should run after the built-in pluginUnitTest suite has run its tests
                        testTask.configure {
                            // shouldRunAfter(settingLogicUnitTest.get().targets.getByName("test").testTask)
                            shouldRunAfter(settingLogicUnitTest)
                        }
                    }
                }
            }

        // Create a new test suite for Functional tests (e.g., TestKit)
        @Suppress("UnstableApiUsage")
        register<JvmTestSuite>("kmpBuildLogicBuildLogicFunctionalTest") {
            description = "Functional tests for the module setting-logic"
            sharedConfig()
            dependencies {
                implementation(gradleTestKit())
                implementation(libs.jetbrainsKotlinTest)
                implementation(libs.junitJupiterApi)
                implementation(libs.junitJupiterParams)
                implementation(platform(libs.junitBom))

                runtimeOnly(libs.junitJupiterEngine)
            }
            targets {
                all {
                    // This test suite should run after the built-in pluginIntegrationTest suite has run its tests
                    testTask.configure {
                        shouldRunAfter(settingLogicIntegrationTest)
                    }
                }
            }
        }
    }
}

logger.info("Step 8 : dependencies")
dependencies {
    implementation(gradleApi())
    implementation(libs.devAdamkoDokkatooPlugin)
    implementation(libs.jetbrainsKotlinGradlePlugin)
    implementation(libs.jetbrainsKotlinxSerializationJson)
    implementation(libs.semver4j)
    implementation(libs.googleCodeGson)
}

logger.info("Step 9 : registration of created plugins")
gradlePlugin {
    plugins {
        register("daemonConfigConventionPlugin") {
            id = "daemon.config.convention.plugin"
            implementationClass = "io.github.dickkouwenhoven.kmpbuildlogic.settings.DaemonConfigConventionPlugin"
        }
        register("kmpBuildLogicGradlePropertiesSettingsConventionPlugin") {
            id = "kmpbuildlogic.properties.convention.plugin"
            implementationClass = "io.github.dickkouwenhoven.kmpbuildlogic.settings.KmpBuildLogicGradlePropertiesSettingsConventionPlugin"
        }
        register("kmpBuildLogicSettingsConventionPlugin") {
            id = "kmpbuildlogic.settings.convention.plugin"
            implementationClass = "io.github.dickkouwenhoven.kmpbuildlogic.settings.KmpBuildLogicSettingsConventionPlugin"
        }
    }
}

logger.info("Step 10: tasks settings")
tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

logger.info("Step 11: Creation of a task which runs all test suites (unit, integration, functional")
val runAllKmpBuildLogicSettingLogicTests =
    tasks.register("runAllKmpBuildLogicSettingLogicTests") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Runs all JVM test suites for setting-logic"
        dependsOn(
            "kmpBuildLogicSettingLogicUnitTest",
            "kmpBuildLogicSettingLogicIntegrationTest",
            "kmpBuildLogicBuildLogicFunctionalTest",
        )
    }

logger.info("Step 12: Linkage of 'runAllKmpBuildLogicSettingLogicTests' to the general ´test' task")
tasks.named("test") {
    dependsOn(runAllKmpBuildLogicSettingLogicTests)
}

logger.info("Step 13: Creation of a verification task for 'runAllKmpBuildLogicSettingLogicTests'")
val verifyKmpBuildLogicSettingLogicTests =
    tasks.register("verifyKmpBuildLogicSettingLogicTests") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Runs and verifies all setting-logic test suites"
        dependsOn(runAllKmpBuildLogicSettingLogicTests)
    }

logger.info("Step 14: Linkage of 'verifySettingLogicTests' to the general 'check' task")
tasks.named("check") {
    dependsOn(verifyKmpBuildLogicSettingLogicTests)
}

logger.info("\n")
logger.info("    ==========             End of build.gradle.kts from setting-logic/settings-conventions             ==========\n")
