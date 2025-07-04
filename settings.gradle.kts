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
 * What is this file?
 *
 * This is settings.gradle.kts, which initializes the Gradle environment before any builds happen.
 * It is used to set up plugin repositories, apply plugins, define version catalogs, manage repositories,
 * and configure build features like build scan and build cache.
 *
 */
logger.info("\n")
logger.info("    ==========                                   INITIALIZATION PHASE                                    ==========\n")
logger.info("    ==========                       Start of settings.gradle.kts from module root                       ==========\n")

logger.info("Step 1 : pluginManagement settings.")
/**
 * Plugin management section:
 *
 * - Sets the GitHub Url for internal plugin resolution.
 * - Includes composite builds for 'setting-logic' and 'build-logic'.
 * - Declares plugins and manages conditional logic (e.q. Foojay only if needed).
 *
 */
pluginManagement {
    val githubUrlProperty = "kmpbuildlogic.properties.github.url"
    val githubUrl =
        providers
            .gradleProperty(githubUrlProperty)
            .orNull ?: throw IllegalArgumentException(
                "GitHub URL is not specified in gradle.properties"
            )
    System.setProperty(githubUrlProperty, githubUrl)

    includeBuild("setting-logic")
    includeBuild("build-logic")

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        // https://github.com/michel-kraemer/gradle-download-task/releases
        id("de.undercouch.download") version System.getProperty("de.undercouch.download.version")
        // https://github.com/gradle/foojay-toolchains/tags
        if (System.getProperty("foojayPluginNeeded").toBoolean()) {
            id("org.gradle.toolchains.foojay.resolver.convention") version System
                .getProperty("org.gradle.toolchains.foojay.resolver.convention")
        }
        id("kmpbuildlogic.settings.convention.plugin")
        id("kmpbuildlogic.project.convention.plugin")
    }
}

logger.info("Step 2 : plugin settings.")
/**
 * Plugins section:
 *
 * - Declares plugins.
 *
 */
plugins {
    // https://gradle.com/develocity/releases/
    val develocityProperty = "org.gradle.develocity"
    val develocityVersion = when {
        settings.providers.gradleProperty(develocityProperty).isPresent -> settings.providers.gradleProperty(develocityProperty).get()
        settings.providers.systemProperty(develocityProperty).isPresent -> settings.providers.systemProperty(develocityProperty).get()
        settings.providers.environmentVariable(develocityProperty).isPresent -> settings.providers.environmentVariable(develocityProperty).get()
        else -> "4.0.2"
    }
    id("com.gradle.develocity") version develocityVersion
    id("kmpbuildlogic.settings.convention.plugin")
}

logger.info("Step 3 : 'rootProject name' setting.")
/**
 * RootProject name:
 *
 * - Root project name configured from properties.
 * - Root project name configured as system property. Will be part of module project name.
 *
 */
rootProject.name = System.getProperty("kmpbuildlogic.properties.root.project.name")

logger.info("Step 4 : 'rootProject buildFileName' setting.")
/**
 * RootProject buildFileName:
 *
 * - Root project buildFileName configured from properties
 *
 */
rootProject.buildFileName = System.getProperty("kmpbuildlogic.properties.root.project.build.file.name")

logger.info("Step 5 : setting of the 'repositories' within 'dependencyResolutionManagement'.")
/**
 * Dependency Resolution Management:
 *
 * - Defines where dependencies are downloaded from.
 *
 */
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(
        RepositoriesMode.FAIL_ON_PROJECT_REPOS,
    )
    repositories {
        // First step is to retrieve the settings for all the variables used within this repository block.
        // A setting can be found within the 'gradle.properties' file or is being set through a system environment setting.
        // Tries to read from gradle.properties first, then falls back to environment variables.
        // The check if a value is found or not is done through '?:'.
        val gitHubUsername: String? =
            settings.providers.gradleProperty("kmpbuildlogic.properties.github.username").orNull ?: System.getenv("GITHUB_USERNAME")
        val gitHubToken: String? = settings.providers.gradleProperty("kmpbuildlogic.properties.github.token").orNull ?: System.getenv("GITHUB_TOKEN")
        val gitHubUrl: String =
            settings.providers.gradleProperty("kmpbuildlogic.properties.github.url").orNull ?: throw IllegalArgumentException(
                "Github URL is not specified in gradle.properties",
            )
        google {
            mavenContent {
                includeGroupByRegex(".*androidx.*")
                includeGroupByRegex(".*android.*")
                includeGroupByRegex(".*google.*")
            }
        }
        gradlePluginPortal()
        maven {
            url = uri(gitHubUrl)
            credentials {
                username = gitHubUsername ?: throw IllegalArgumentException(
                    "Github Username is not specified in gradle.properties or not specified as an environment variable",
                )
                password = gitHubToken ?: throw IllegalArgumentException(
                    "Github Token is not specified in gradle.properties or not specified as an environment variable",
                )
            }
        }
        mavenCentral()
    }

    /**
     * Version Catalogs:
     *
     * - Central files that define dependency versions.
     *
     */
    versionCatalogs {
        create("alpha") {
            from(files("gradle/alpha.versions.toml"))
        }
        create("beta") {
            from(files("gradle/beta.versions.toml"))
        }
        create("rc") {
            from(files("gradle/rc.versions.toml"))
        }
    }
}

logger.info("Step 6 : retrieve and setting of the variables needed for the buildscan feature within develocity.")
/**
 * Build Scan:
 *
 * - Build scan legal handling and build metadata.
 *
 */
val gradleBuildScanLegalTermsOfUseUrl = System.getProperty("org.gradle.buildscan.legal.terms.of.use.url")
val gradleBuildScanLegalTermsOfUseAutomaticAcceptance =
    System.getProperty("org.gradle.buildscan.legal.terms.of.use.automatic.acceptance")
val isCLI = System.getenv("CI") != null
val buildScanUploadInBackground = System.getProperty("kmpbuildlogic.properties.buildscan.upload.in.background")
val kmpBuildLogicLibraryVersion = System.getProperty("kmpbuildlogic.properties.library.version")
val kmpBuildLogicBuildNumber = System.getProperty("kmpbuildlogic.properties.build.number")

logger.info("Step 7 : implementation of develocity settings.")
/**
 * Develocity:
 *
 * - Applying Develocity settings.
 *
 */
develocity {
    buildScan {
        termsOfUseUrl.set(gradleBuildScanLegalTermsOfUseUrl)
        termsOfUseAgree.set(gradleBuildScanLegalTermsOfUseAutomaticAcceptance)
        if (isCLI) {
            publishing.onlyIf { true }
        }
        uploadInBackground.set(buildScanUploadInBackground.toBoolean())
        tag("CLI")
        tag(System.getProperty("os.name"))
        value("Library Version: ", kmpBuildLogicLibraryVersion)
        value("Build Number: ", kmpBuildLogicBuildNumber)
    }
}

logger.info("Step 8 : retrieve and setting of the variables needed for 'build cache' settings.")
/**
 * Build Cache:
 *
 * - Applying Build cache settings.
 * - Speeds up builds by reusing outputs.
 *
 */
val localBuildCacheDirectory = System.getProperty("kmpbuildlogic.properties.local.build.cache.path")
val localBuildCacheEnabled = System.getProperty("kmpbuildlogic.properties.local.build.cache.enabled")

logger.info("Step 9 : buildCache")
buildCache {
    local {
        isEnabled = localBuildCacheEnabled.toBoolean()
        if (localBuildCacheDirectory != "") {
            directory = localBuildCacheDirectory
        }
    }
}

logger.info("Step 10: 'enableFeaturePreview' settings.")
/**
 * Feature previews:
 *
 * - Feature previews enabled.
 * - Enables experimental Gradle Features
 *
 */
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

logger.info("Step 11: including the modules used in this project.")
/**
 * Child projects:
 *
 * - Child projects included.
 *
 */
include(":documentation")
include(":childProjectA")
include(":childProjectB")

logger.info("\n")
logger.info("    ==========                        End of settings.gradle.kts from module root                        ==========\n")
logger.info("    ==========                                     CONFIGURATION PHASE                                   ==========\n")
