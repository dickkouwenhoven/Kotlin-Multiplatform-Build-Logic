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
    // need to set variable 'githubUrl' because of usage within the module 'build-logic'
    val githubUrlProperty = "kmpbuildlogic.properties.github.url"
    val githubUrlRaw = 
        providers
            .gradleProperty(githubUrlProperty)
            .orNull ?:  throw IllegalArgumentException(
                "Github URL is not specified in gradle.properties",
            )
    val githubUrl = githubUrlRaw
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
        val undercouchVersion = providers.gradleProperty("de.undercouch.download.version").orNull ?: "5.6.0"
        id("de.undercouch.download") version undercouchVersion
        // https://github.com/gradle/foojay-toolchains/tags
        val foojayVersion = providers.gradleProperty("org.gradle.toolchains.foojay.resolver.convention").orNull ?: "1.0.0"
        val foojayPluginNeeded = System.getProperty("foojayPluginNeeded").toBoolean()
        if (foojayPluginNeeded) {
            id("org.gradle.toolchains.foojay-resolver-convention") version foojayVersion
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
    id("com.gradle.develocity") version "4.0.2"
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
val propertiesRootProjectName = providers.gradleProperty("kmpbuildlogic.properties.root.project.name").orNull ?: "KMP-Build-Logic"
rootProject.name = propertiesRootProjectName

logger.info("Step 4 : 'rootProject buildFileName' setting.")
/**
 * RootProject buildFileName:
 *
 * - Root project buildFileName configured from properties
 *
 */
val propertiesRootProjectBuilFileName = providers.gradleProperty("kkmpbuildlogic.properties.root.project.build.file.name").orNull ?: "build.gradle.kts"
rootProject.buildFileName = propertiesRootProjectBuilFileName

logger.info("Step 5 : setting of the 'main rootProject name' also as 'system property'.")
// The design setup of a module project name is: 'main rootProject name' + '-' + 'moduleName'
System.setProperty("kmpbuildlogic.properties.root.project.name", propertiesRootProjectName)

logger.info("Step 6 : setting of the 'repositories' within 'dependencyResolutionManagement'.")
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
            providers.gradleProperty("kmpbuildlogic.properties.github.username").orNull ?: System.getenv("GITHUB_USERNAME")
        val gitHubToken: String? = providers.gradleProperty("kmpbuildlogic.properties.github.token").orNull ?: System.getenv("GITHUB_TOKEN")
        val gitHubUrl: String =
            providers.gradleProperty("kmpbuildlogic.properties.github.url").orNull ?: throw IllegalArgumentException(
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

logger.info("Step 7 : retrieve and setting of the variables needed for the buildscan feature within develocity.")
/**
 * Build Scan:
 *
 * - Build scan legal handling and build metadata. 
 *
 */
val gradleBuildScanLegalTermsOfUseUrl = providers.gradleProperty("org.gradle.buildscan.legal.terms.of.use.url").orNull ?: "https://gradle.com/help/legal-terms-of-use"
val gradleBuildScanLegalTermsOfUseAutomaticAcceptance =
    providers.gradleProperty("org.gradle.buildscan.legal.terms.of.use.automatic.acceptance").orNull ?: "no"
val isCLI = System.getenv("CI") != null
val buildScanUploadInBackground = providers.gradleProperty("kmpbuildlogic.properties.buildscan.upload.in.background").orNull ?: "false"
val kmpbuildlogicLibraryVersion = providers.gradleProperty("kmpbuildlogic.properties.library.version").orNull ?: "1.0.0"
val kmpbuildlogicBuildNumber = providers.gradleProperty("kmpbuildlogic.properties.build.number").orNull ?: "2025.05.26.01"

logger.info("Step 8 : implementation of develocity settings.")
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
        value("Library Version: ", kmpbuildlogicLibraryVersion)
        value("Build Number: ", kmpbuildlogicBuildNumber)
    }
}

logger.info("Step 9 : retrieve and setting of the variables needed for 'build cache' settings.")
/**
 * Build Cache:
 *
 * - Applying Build cache settings.
 * - Speeds up builds by reusing outputs.
 *
 */
val localBuildCacheDirectory =
    providers.gradleProperty("kmpbuildlogic.properties.local.build.cache.path").get()
val localBuildCacheEnabled = providers.gradleProperty("kmpbuildlogic.properties.local.build.cache.enabled").orNull ?: "false"

logger.info("Step 10: buildCache")
buildCache {
    local {
        isEnabled = localBuildCacheEnabled.toBoolean()
        if (localBuildCacheDirectory != "") {
            directory = localBuildCacheDirectory
        }       
    }
}

logger.info("Step 11: setting of 'kmpbuildlogicLibraryVersion' as a 'system property'.")
/**
 * Library metadata:
 *
 * - Library metadata shared. 
 *
 */
System.setProperty("kmpbuildlogic.properties.library.version", kmpbuildlogicLibraryVersion)

logger.info("Step 12: setting of 'packageName' as a 'system property'.")
/**
 * Package name:
 *
 * - Package name shared. 
 *
 */
val kmpbuildlogicPackageName = providers.gradleProperty("kmpbuildlogic.properties.package.name").orNull ?: "io.github.dickkouwenhoven.kmpbuildlogic"
System.setProperty("kmpbuildlogic.properties.package.name", kmpbuildlogicPackageName)

logger.info("Step 13: 'enableFeaturePreview' settings.")
/**
 * Feature previews:
 *
 * - Feature previews enabled.
 * - Enables experimental Gradle Features
 *
 */
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

logger.info("Step 14: including the modules used in this project.")
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
