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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
logger.info("\n")
logger.info("    ==========                          Start of build.gradle.kts from plugin                          ==========\n")
logger.info("Step 1 : applying plugins")
plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins.
    // Note: from version 1.0.0 this plugin is automatically added by the 'gradle publish plugin'.
    `java-gradle-plugin`

    // Apply the Java Library plugin to add support for publishing a Java Library
    `java-library`

    // Apply the Kotlin Dsl plugin to add support for WRITING Gradle build scripts in Kotlin instead of Groovy.
    // Scope here: define plugins, tasks and configuration in Kotlin.
    `kotlin-dsl`

    // Apply the Maven Publish plugin for generating the publication metadata for Gradle Plugins.
    `maven-publish`

    // Apply the Gradle Signing plugin to add support for sign artifacts and metadata files
    signing

    // Apply the shadow plugin to add automatic creation fat JARs
    alias(libs.plugins.gradleupShadowPlugin)

    // Apply the JVM Test Suite plugin
    `jvm-test-suite`

    id("kmpbuildlogic.project.convention.plugin")
}

logger.info("Step 2 : define task groups variables")
val kmpBuildLogicPluginCheckGroup = "KMP Build Logic Plugin Check"
val kmpBuildLogicPluginTestGroup = "KMP Build Logic Plugin Test"

logger.info("Step 3 : creation of mockitoAgent")
val mockitoAgent : Configuration = configurations.create("mockitoAgent")

logger.info("Step 4 : Generate plugin classpath file")
val generatePluginClasspathFile by tasks.registering {
    group = kmpBuildLogicPluginTestGroup
    description = "Generates plugin classpath file for integration tests"

    val runtimeClasspath: FileCollection = configurations.runtimeClasspath.get()
    val outputFile = layout.buildDirectory.file("plugin-classpath.txt")

    inputs.files(configurations.runtimeClasspath)
    outputs.file(outputFile)

    doLast {
        val file = outputFile.get().asFile
        file.printWriter().use { writer ->
            runtimeClasspath.forEach { file ->
                writer.println(file.absolutePath)
            }
        }
        logger.info("Generated plugin classpath file at: ${outputFile.get().asFile}")
    }
}

logger.info("Step 5 : testing settings")
testing {
    suites {
        val kotlinLanguageVersion: String = System.getProperty("kotlin.language.version")

        // Shared configuration for all test suites
        @Suppress("UnstableApiUsage")
        val sharedConfig: JvmTestSuite.() -> Unit = {
            dependencies {
                implementation(project())
                implementation(libs.jetbrainsKotlinTestJunit5)
                implementation(libs.junitJupiter)
                implementation(libs.mockitoCore)
                implementation(projects.publish.gradlePortalPlugin)
                implementation(libs.apacheMavenModel)
                implementation(libs.autonomousAppTestKitTruth)
                implementation(libs.googleTestParameterInjector)
                implementation(libs.googleTruth)
            }
            targets {
                all {
                    @Suppress("UnstableApiUsage")
                    testTask.configure {
                        useJUnitPlatform()
                        @Suppress("UnstableApiUsage")
                        useKotlinTest(kotlinLanguageVersion)
                        shouldRunAfter("test") // Prevent conflict with default 'test' task
                        group = kmpBuildLogicPluginTestGroup
                        jvmArgs("-javaagent:${mockitoAgent.singleFile.absolutePath}") // Use mockitoAgent
                        jvmArgs("-XX:+EnableDynamicAgentLoading")
                    }
                }
            }
        }

        // Create a new test suite for Unit tests
        @Suppress("UnstableApiUsage")
        val kmpBuildLogicPluginUnitTest =
            register<JvmTestSuite>("kmpBuildLogicPluginUnitTest") {
                description = "Unit tests for the module plugin"
                sharedConfig()
            }

        // Create a new test suite for Integration tests
        @Suppress("UnstableApiUsage")
        val kmpBuildLogicPluginIntegrationTest =
            register<JvmTestSuite>("kmpBuildLogicPluginIntegrationTest") {
                description = "Integration tests for the module plugin"
                sharedConfig()
                targets {
                    all {
                        // This test suite should run after the built-in pluginUnitTest suite has run its tests
                        testTask.configure {
                            shouldRunAfter(kmpBuildLogicPluginUnitTest)
                            // dependsOn(generatePluginClassPathFile)
                            systemProperty(
                                "plugin-classpath-file",
                                layout
                                    .buildDirectory
                                    .file("plugin-classpath.txt")
                                    .get()
                                    .asFile
                                    .absolutePath,
                            )
                        }
                    }
                }
            }

        // Create a new test suite for Functional tests (e.g., TestKit)
        @Suppress("UnstableApiUsage")
        register<JvmTestSuite>("kmpBuildLogicPluginFunctionalTest") {
            description = "Functional tests for the module plugin"
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
                        shouldRunAfter(kmpBuildLogicPluginIntegrationTest)
                    }
                }
            }
        }
    }
}

logger.info("Step 6 : adding dependencies")
dependencies {
    api(gradleApi())
    api(libs.jetbrainsKotlinStdlib)

    compileOnly(libs.androidToolsBuildGradle)
    compileOnly(libs.jetbrainsKotlinGradlePlugin)

    implementation(libs.jetbrainsKotlinReflect)
    implementation(libs.jetbrainsKotlinStdlibJdk8)

    mockitoAgent(libs.mockitoCore) { isTransitive = false }

    implementation(projects.annotations)
    implementation(projects.publish.gradlePortalPlugin)
}

logger.info("Step 8 : creation of publishPlugin")
gradlePlugin {
    plugins {
        create("gradlePortalPublishingPlugin") {
            id = "io.github.dickkouwenhoven.kmpbuildlogic.gradlePortalPublishingPlugin"
            implementationClass = "io.github.dickkouwenhoven.kmpbuildlogic.publish.GradlePortalPublishingPlugin"
            displayName = "Gradle Portal Publish Plugin"
            description = "Gradle Portal Plugin that configures tasks to enable automatically upload of libraries to the Gradle Portal Repository"
        }
        create("mavenCentralPublishingPlugin") {
            id = "io.github.dickkouwenhoven.kmpbuildlogic.mavenCentralPublishingPlugin"
            implementationClass = "io.github.dickkouwenhoven.kmpbuildlogic.publish.MavenCentralPublishingPlugin"
            displayName = "Maven Central Publish Plugin"
            description = "Maven Central Plugin that configures tasks to enable automatically upload of libraries to the Maven Central Repository"
        }
        create("mavenLocalPublishingPlugin") {
            id = "io.github.dickkouwenhoven.kmpbuildlogic.mavenLocalPublishingPlugin"
            implementationClass = "io.github.dickkouwenhoven.kmpbuildlogic.publish.MavenLocalPublishingPlugin"
            displayName = "Maven Local Publish Plugin"
            description = "Maven Local Plugin that configures tasks to enable automatically upload of libraries to the Maven Local Repository"
        }
        create("publishingPlugin") {
            id = "io.github.dickkouwenhoven.kmpbuildlogic.publishingPlugin"
            implementationClass = "io.github.dickkouwenhoven.kmpbuildlogic.publish.publishingPlugin"
            displayName = "Kmpbuildlogic Publish Plugin"
            description = "Plugin that configures tasks to enable automatically upload of libraries to the repositories:" +
                "'Maven Local','Gradle Portal' and 'Maven Central'"
        }
    }
    isAutomatedPublishing = false
}

logger.info("Step 9 : define group name")
val kmpbuildlogicPackageName: String? = System.getProperty("kmpbuildlogic.properties.package.name")
val groupName = "$kmpbuildlogicPackageName.plugin"
group = groupName

logger.info("Step 10: define version")
version = System.getProperty("kmpbuildlogic.properties.library.version")

logger.info("Step 11: define description")
description = "This is a module that creates publishing plugins as defined in this project (Gradle Portal Plugin and Maven Central Plugin)"

logger.info("Step 12: adding 'kmpBuildLogicPluginFunctionalTest' as a dependency for 'check' task")
tasks.named("check") {
    group = kmpBuildLogicPluginCheckGroup
    @Suppress("UnstableApiUsage")
    dependsOn(testing.suites.named("kmpBuildLogicPluginUnitTest"))
    @Suppress("UnstableApiUsage")
    dependsOn(testing.suites.named("kmpBuildLogicPluginIntegrationTest"))
    @Suppress("UnstableApiUsage")
    dependsOn(testing.suites.named("kmpBuildLogicPluginFunctionalTest"))
}

logger.info("Step 13 : Java settings added")
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

logger.info("Step 14 : making use of 'JUnitPlatform' when a task of type 'test' is used")
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

logger.info("Step 15: define the settings for shadowJar")
tasks.shadowJar {
    archiveClassifier = ""
}

logger.info("Step 16: dokkatoo settings")
dokkatoo {
    dokkatooPublications.configureEach {
        // Include the doc to be able to create a module description
        includes.from(file("docs/ModulePlugin.md"))
    }

    // Creation of reference name for this module
    moduleName.set("KMP Build Logic Plugin")
}
logger.info("Step 17: define pom url")
val kmpbuildlogicPublishGithubUrl: String? = System.getProperty("kmpbuildlogic.publish.github.url")
logger.debug("Pom url is: $kmpbuildlogicPublishGithubUrl")

logger.info("Step 18: retrieve sonatype login credentials")
val sonatypeUsername: String? = System.getenv("SONATYPE_USERNAME")
val sonatypePassword: String? = System.getenv("SONATYPE_PASSWORD")

logger.info("Step 19: publishing of the plugin")
publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            from(components["java"])
            groupId = "com.dickkouwenhoven.kmpbuildlogic"
            artifactId = "publishingPlugin"
            // version = kmpbuildlogicPublishLibraryVersion
            // versionMapping has the preference
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

            pom {
                name = "KMP Build Logic Publishing Plugin"
                description = "A plugin to simplify publishing configurations in Kotlin MultiPlatform projects."
                url = kmpbuildlogicPublishGithubUrl
                // properties can be added
                // properties =
                // mapOf(
                //        "myProp" to "value",
                //        "prop.with.dots" to "anotherValue",
                //    )
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                    }
                }
                developers {
                    developer {
                        id = "dickkouwenhoven"
                        name = "Dick Kouwenhoven"
                        email = "dick.kouwenhoven@icloud.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/dickkouwenhoven/Kotlin-Multiplatform-Build-Logic.git"
                    developerConnection = "scm:git:ssh://github.com/dickkouwenhoven/Kotlin-Multiplatform-Build-Logic.git"
                    url = kmpbuildlogicPublishGithubUrl
                }
            }
        }
    }

    repositories {
        // Enablement for test locally before publishing externally by publishing to mavenLocal
        // defining mavenLocal() isn´t needed anymore, because the plugin automatically creates a task
        // publishToMavenLocal
        maven {
            // added val urls
            val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
            val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
            name = "MavenCentral"
            // original url
            // url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            // added url
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = sonatypeUsername ?: throw IllegalArgumentException(
                    "SonaTypeUserName is not specified as system environment variable",
                )
                password = sonatypePassword ?: throw IllegalArgumentException(
                    "SonaTypePassword is not specified as system environment variable",
                )
            }
        }
    }
}

logger.info("Step 20: signing the publishing plugin")
signing {
    useInMemoryPgpKeys(
        System.getenv("GPG_SECRET_KEY_KMPBUILDLOGIC_PUBLISHING_PLUGIN_ASCII"),
        System.getenv("GPG_SECRET_KEY_KMPBUILDLOGIC_PUBLISHING_PLUGIN_PASSPHRASE"),
    )
    sign(publishing.publications["pluginMaven"])
}

logger.info("\n")
logger.info("    ==========                           End of build.gradle.kts from plugin                           ==========\n")
