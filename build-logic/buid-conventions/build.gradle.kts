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
logger.info("    ==========               Start of build.gradle.kts from build-logic/build-conventions              ==========\n")

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
}

logger.info("Step 2 : define group name")
val kmpbuildlogicPackageName: String? = System.getProperty("kmpbuildlogic.properties.package.name")
val groupName = "$kmpbuildlogicPackageName.build-logic"
group = groupName

logger.info("Step 3 : define version")
version = System.getProperty("kmpbuildlogic.properties.library.version")

logger.info("Step 4 : define description")
description = "This is a standard module to enable created convention plugins to be used in this project"

logger.info("Step 5 : define task groups variables")
val kmpBuildLogicBuildLogicTestGroup = "KMP Build Logic Build Logic Test"

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
                        group = kmpBuildLogicBuildLogicTestGroup
                    }
                }
            }
        }

        // Create a new test suite for Unit tests
        @Suppress("UnstableApiUsage")
        val buildLogicUnitTest =
            register<JvmTestSuite>("kmpBuildLogicBuildLogicUnitTest") {
                description = "Unit tests for the module build-logic"
                sharedConfig()
            }

        // Create a new test suite for Integration tests
        @Suppress("UnstableApiUsage")
        val buildLogicIntegrationTest =
            register<JvmTestSuite>("kmpBuildLogicBuildLogicIntegrationTest") {
                description = "Integration tests for the module build-logic"
                sharedConfig()
                targets {
                    all {
                        // This test suite should run after the built-in pluginUnitTest suite has run its tests
                        testTask.configure {
                            shouldRunAfter(buildLogicUnitTest)
                        }
                    }
                }
            }

        // Create a new test suite for Functional tests (e.g., TestKit)
        @Suppress("UnstableApiUsage")
        register<JvmTestSuite>("kmpBuildLogicBuildLogicFunctionalTest") {
            description = "Functional tests for the module build-logic"
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
                        shouldRunAfter(buildLogicIntegrationTest)
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
    // implementation(libs.kmpBuildLogicPublishPlugin)
    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

logger.info("Step 10: creation and settings of tasks")
tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

// Combine all tests into one 'all tests' task.
tasks.register("kmpBuildLogicBuildLogicAllTests") {
    group = kmpBuildLogicBuildLogicTestGroup
    description = "Runs all test suites (unit, integration, functional) for build-logic"
    dependsOn(
        "kmpBuildLogicBuildLogicUnitTest",
        "kmpBuildLogicBuildLogicIntegrationTest",
        "kmpBuildLogicBuildLogicFunctionalTest",
    )
}

// Creation of a check task for the module build-logic.
tasks.register("kmpBuildLogicBuildLogicChecks") {
    group = "KMP Build Logic Build Logic Check"
    description = "Runs all checks for build-logic"
    dependsOn("kmpBuildLogicBuildLogicAllTests")
}

// Add the 'kmpBuildLogicBuildLogicChecks' task to the general 'check' task to make sure it runs during a compilation ('./gradlew build')
tasks.named("check") {
    dependsOn("kmpBuildLogicBuildLogicChecks")
}

logger.info("Step 11: registration of created plugins")
gradlePlugin {
    plugins {
        register("dokkatooConventionPlugin") {
            id = "dokkatoo.convention.plugin"
            implementationClass = "DokkatooConventionPlugin"
        }
        register("jvmToolchainEvaluateConventionPlugin") {
            id = "jvm.toolchain.evaluate.convention.plugin"
            implementationClass = "JvmToolchainEvaluateConventionPlugin"
        }
        register("kmpbuildlogicProjectConventionPlugin") {
            id = "kmpbuildlogic.project.convention.plugin"
            implementationClass = "KmpBuildLogicProjectConventionPlugin"
        }
        register("verifyAllVersionsCatalogs") {
            id = "kmpbuildlogic.verification.convention.plugin"
            implementationClass = "VerifyAllVersionsCatalogsConventionPlugin"
        }
    }
}

logger.info("\n")
logger.info("    ==========                End of build.gradle.kts from build-logic/build-conventions               ==========\n")
