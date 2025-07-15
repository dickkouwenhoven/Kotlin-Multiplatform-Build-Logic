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
 * This is settings.gradle.kts of the child module setting-logic.
 * Like the root settings.gradle.kts it also initializes the Gradle environment before any builds happen., but it does more.
 * It is especially used to set up the environment as such that the requirements of KMP Build Logic are met, but taken into account user preferences.
 *
 */
import java.util.Locale
import java.util.Properties

logger.info("\n")
logger.info("    ==========                               INITIALIZATION PHASE OF LOGIC                               ==========\n")
logger.info("    ==========                      Start of settings.gradle.kts from setting-logic                      ==========\n")

logger.info("Step 1 : Creation of property resolve function.")

/**
 * [resolveProperty] function:
 *
 * A general function to 'read' the gradleProperty.
 *
 * - Priority sequence used is: CLI > systemProperty > gradle.properties > environment
 *
 */
private fun resolveProperty(key: String): String? {
    val rootGradleProperties =
        file("../gradle.properties")
            .takeIf { it.exists() }
            ?.inputStream()
            ?.use { Properties().apply { load(it) } }
    return when {
        settings.providers.gradleProperty(key).isPresent -> settings.providers.gradleProperty(key).get()
        // Sometimes gradle.properties file is not available in the early cycle. Reading it directly therefore needed.
        rootGradleProperties?.getProperty(key) != null -> rootGradleProperties.getProperty(key)
        settings.providers.systemProperty(key).isPresent -> settings.providers.systemProperty(key).get()
        settings.providers.environmentVariable(key).isPresent -> settings.providers.environmentVariable(key).get()
        else -> null
    }
}

logger.info("Step 2 : Determine which which version catalog to use.")
/**
 * Version Profile section:
 *
 * - Sets the to be used library as defined in the 'gradle.properties' file.
 * - Libraries to choose from are: Alpha Version, Beta Version, Release Candidate Version or General Availability Version.
 * - Declares plugins and manages conditional logic (e.q. Foojay only if needed).
 *
 */
val versionProfileProperty = "kmpbuildlogic.properties.version.profile"
val versionProfileDefaultValue = "ga"
var selectedVersionProfile: String = resolveProperty(versionProfileProperty) ?: versionProfileDefaultValue
System.setProperty("kmpbuildlogic.properties.version.profile", selectedVersionProfile)

logger.info("Step 3 : Determine which name to use for the root project")
/**
 * Root Project Name section:
 *
 * - Sets the name to be used for 'rootProjectName'
 * - The design setup of a module project name is as follows: 'main rootProject name' + '-' + 'moduleName'
 *
 */
val rootProjectNameProperty = "kmpbuildlogic.properties.root.project.name"
val propertiesRootProjectName = resolveProperty(rootProjectNameProperty) ?: "Kotlin-Multiplatform-Build-Logic"
System.setProperty(rootProjectNameProperty, propertiesRootProjectName)

logger.info("Step 4 : Determine which name to use for the root project build file name")
/**
 * Root Project Build File Name section:
 *
 * - Sets the name to be used for 'rootProject.buildFileName'
 *
 */
val rootProjectBuildFileNameProperty = "kmpbuildlogic.properties.root.project.build.file.name"
val propertiesRootProjectBuildFileName = resolveProperty(rootProjectBuildFileNameProperty) ?: "build.gradle.kts"
System.setProperty(rootProjectBuildFileNameProperty, propertiesRootProjectBuildFileName)

logger.info("Step 5 : Determine the settings to be used for develocity build scan")
/**
 * Develocity BuildScan settings section:
 *
 * - Sets the url to be used for the terms of use
 * - Sets a boolean value to be used for the terms of use agree
 * - Sets a boolean value to be used for the upload in background
 * - Sets the version to be used for the library version
 * - Sets the number to be used for the build number
 *
 */
val gradleBuildScanLegalTermsOfUseUrlProperty = "org.gradle.buildscan.legal.terms.of.use.url"
val gradleBuildScanLegalTermsOfUseUrl =
    resolveProperty(
        gradleBuildScanLegalTermsOfUseUrlProperty,
    ) ?: "https://gradle.com/help/legal-terms-of-use"
System.setProperty(gradleBuildScanLegalTermsOfUseUrlProperty, gradleBuildScanLegalTermsOfUseUrl)

val gradleBuildScanLegalTermsOfUseAutomaticResponseProperty =
    "org.gradle.buildscan.legal.terms.of.use.automatic.acceptance"
val gradleBuildScanLegalTermsOfUseAutomaticAcceptance =
    resolveProperty(gradleBuildScanLegalTermsOfUseAutomaticResponseProperty) ?: "no"
System.setProperty(
    gradleBuildScanLegalTermsOfUseAutomaticResponseProperty,
    gradleBuildScanLegalTermsOfUseAutomaticAcceptance,
)

val buildScanUploadInBackgroundProperty = "org.gradle.buildscan.upload.in.background"
val buildScanUploadInBackground = resolveProperty(buildScanUploadInBackgroundProperty) ?: "false"
System.setProperty(
    buildScanUploadInBackgroundProperty,
    buildScanUploadInBackground,
)

val kmpBuildLogicVersionProperty = "kmpbuildlogic.properties.library.version"
val kmpBuildLogicLibraryVersion = resolveProperty(kmpBuildLogicVersionProperty) ?: "1.0.0"
System.setProperty(kmpBuildLogicVersionProperty, kmpBuildLogicLibraryVersion)

val kmpBuildLogicSnapshotVersionProperty = "kmpbuildlogic.properties.snapshot.version"
val kmpBuildLogicSnapshotVersionValue = resolveProperty(kmpBuildLogicSnapshotVersionProperty) ?: "1.0.0-SNAPSHOT"
System.setProperty(kmpBuildLogicSnapshotVersionProperty, kmpBuildLogicSnapshotVersionValue)

val kmpBuildLogicBuildNumberProperty = "kmpbuildlogic.properties.build.number"
val kmpBuildLogicBuildNumber = resolveProperty(kmpBuildLogicBuildNumberProperty) ?: "2025.07.04.01"
System.setProperty(kmpBuildLogicBuildNumberProperty, kmpBuildLogicBuildNumber)

logger.info("Step 6: Determine the settings to be used for buildCache")
/**
 * Build Cache settings section:
 *
 * - Sets a boolean value to be used for enablement
 * - Sets a folder value to be used for local build cache directory
 *
 */
val kmpBuildLogicPropertiesLocalBuildCacheEnabledProperty = "kmpbuildlogic.properties.local.build.cache.enabled"
val localBuildCacheEnabled = resolveProperty(kmpBuildLogicPropertiesLocalBuildCacheEnabledProperty) ?: "false"
System.setProperty(kmpBuildLogicPropertiesLocalBuildCacheEnabledProperty, localBuildCacheEnabled)

val kmpBuildLogicPropertiesLocalBuildCachePathProperty = "kmpbuildlogic.properties.local.build.cache.path"
val localBuildCacheDirectory =
    resolveProperty(kmpBuildLogicPropertiesLocalBuildCachePathProperty) ?: "./build/cache"
System.setProperty(kmpBuildLogicPropertiesLocalBuildCachePathProperty, localBuildCacheDirectory)

logger.info("Step 7: Determine the setting to be used for 'localRepositoryDir'")
/**
 * Local Repository Directory settings section:
 *
 * - Sets the folder value to be used for local repository directory
 *
 */
val kmpBuildLogicPropertiesLocalRepositoryPathProperty = "kmpbuildlogic.properties.local.repository.path"
val kmpBuildLogicPropertiesLocalRepositoryPathValue =
    resolveProperty(kmpBuildLogicPropertiesLocalRepositoryPathProperty) ?: "./build/repository"
System.setProperty(kmpBuildLogicPropertiesLocalRepositoryPathProperty, kmpBuildLogicPropertiesLocalRepositoryPathValue)

logger.info("Step 8: setting of 'packageName' as a 'system property'.")
/**
 * Package name:
 *
 * - Package name shared.
 *
 */
val kmpBuildLogicPropertiesPackageNameProperty = "kmpbuildlogic.properties.package.name"
val kmpbuildlogicPackageName =
    resolveProperty(
        kmpBuildLogicPropertiesPackageNameProperty,
    ) ?: "io.github.dickkouwenhoven.kmpbuildlogic"
System.setProperty(kmpBuildLogicPropertiesPackageNameProperty, kmpbuildlogicPackageName)

logger.info("Step 9: setting of 'kotlinLanguageVersion' as a 'system property'.")
/**
 * Kotlin Language Version settings section:
 *
 * - Set the version to be used for the koling lanaguage version
 *
 */
val kotlinLanguageVersionProperty = "kotlin.language.version"
val kotlinLanguageVersion =
    resolveProperty(
        kotlinLanguageVersionProperty,
    ) ?: "2.1.21"
System.setProperty(kotlinLanguageVersionProperty, kotlinLanguageVersion)

logger.info("Step 10: setting of the 'repositories' within 'dependencyResolutionManagement'.")
/**
 * Dependency Resolution Management:
 *
 * - Defines where dependencies are downloaded from.
 * - Activation of 'libs' library based upon chosen library version.
 *
 */
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    val fileName =
        when (selectedVersionProfile) {
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

logger.info("Step 11: 'rootProject name' setting.")
/**
 * RootProject name:
 *
 * - Root project name configuration setting based upon a combination of main rootProject name and the module project name.
 *
 */
rootProject.name = "$propertiesRootProjectName-Setting-Logic"

logger.info("Step 12 : include the module 'settings-conventions.")
/**
 * Child projects:
 *
 * - Child projects included.
 *
 */
include(":settings-conventions")

logger.info("Step 13 : Creation of the environment compatibility checker.")

/**
 * [EnvironmentCompatibilityChecker]:
 *
 * - Compatibility check for Java Runtime Version and Gradle Version.
 * - Compatibility check for Operating System Version and Gradle Version.
 * - Compatibility check for KMP Build Logic requirement and Gradle Version.
 * - Compatibility check for toolchain support requirement in combination with Java Runtime Version and Gradle Version.
 *
 */
object EnvironmentCompatibilityChecker {
    private val logger: Logger = Logging.getLogger(javaClass)

    // Symbols used
    private const val ATTENTION_SYMBOL = "⚠️"
    private const val ERROR_SYMBOL = "❌"
    private const val INFORM_SYMBOL = "ℹ️"
    private const val MARK_SYMBOL = "📍"
    private const val POSITIVE_CHECK_SYMBOL = "✅"
    private const val PROCESS_SYMBOL = "⏳"
    private const val SETTINGS_SYMBOL = "🔧"

    // Special characters used
    private const val RETURN = "\n"
    private const val SPACE = "\u0020"
    private const val TAB = "\t"

    // Combined strings
    const val ERROR_STRING = "$TAB$ERROR_SYMBOL$SPACE"
    const val INFORM_STRING = "$TAB$INFORM_SYMBOL$SPACE"
    const val INFORM_RESULT_STRING = "$INFORM_SYMBOL$SPACE"
    const val MARK_STRING = "$TAB$MARK_SYMBOL$SPACE"
    const val POS_STRING = "$TAB$POSITIVE_CHECK_SYMBOL$SPACE"
    const val POS_RESULT_STRING = "$POSITIVE_CHECK_SYMBOL$SPACE"
    const val POS_RESULT_STRING_WITH_RETURN = "$RETURN$POSITIVE_CHECK_SYMBOL$SPACE"
    const val PROCESS_STRING = "$TAB$PROCESS_SYMBOL$SPACE"
    const val SETTING_STRING = "$TAB${SETTINGS_SYMBOL}$SPACE"
    const val WARN_STRING = "$TAB$ATTENTION_SYMBOL$SPACE"
    const val WARN_STRING_WITH_RETURN = "$RETURN$TAB$ATTENTION_SYMBOL$SPACE"
    const val WARN_RESULT_STRING = "$ATTENTION_SYMBOL$SPACE"

    /**
     * Function [run]:
     *
     * - Overall function that combines all child functions together.
     * - Is the function that a plugin uses to start these compatibility checks
     *
     */
    fun run(settings: Settings): Boolean {
        val javaCheck = checkJavaRuntimeAndGradleVersionOrWarn(settings)
        if (javaCheck.result) {
            logger.lifecycle("\n")
            logger.lifecycle(
                "$POS_RESULT_STRING Compatibility check passed for Java Runtime ${javaCheck.javaRuntimeMajor} and ${javaCheck.gradleVersion}.",
            )
        } else {
            logger.lifecycle(
                "\n$WARN_RESULT_STRING Compatibility check did not passed all checks for Java Runtime ${javaCheck.javaRuntimeMajor} and ${javaCheck.gradleVersion}.",
            )
        }
        val osInfo = checkOsOrWarn()
        return javaCheck.result && osInfo.result
    }

    /**
     * Function [checkJavaRuntimeAndGradleVersionOrWarn]:
     *
     * - Does the 'Compatibility check for Java Runtime Version and Gradle Version.'.
     *
     */
    private fun checkJavaRuntimeAndGradleVersionOrWarn(settings: Settings): JavaRuntimeGradleVersionInfo {
        // Website of Gradle Releases is: https://github.com/gradle/gradle/releases
        val gradleVersion = GradleVersion.version(settings.gradle.gradleVersion)
        val gradleVersionString = gradleVersionToString(gradleVersion.toString())

        val javaRuntimeVersion: String = System.getProperty("java.version")
        val javaRuntimeMajor = parseJavaMajor(javaRuntimeVersion)

        logger.lifecycle("\nCompatibility check for Java Runtime and Gradle.\n")
        logger.lifecycle("$MARK_STRING Java Runtime: $javaRuntimeVersion (parsed major: $javaRuntimeMajor)")
        logger.lifecycle("$MARK_STRING Gradle Version: $gradleVersionString")

        // --- Compatibility information warning ---
        val compatibility =
            javaRuntimeMatrix[javaRuntimeMajor] ?: run {
                logger.warn("$WARN_STRING Java $javaRuntimeMajor is unknown in compatibility table. Proceeding without further checking.")
                return JavaRuntimeGradleVersionInfo(javaRuntimeMajor, gradleVersion, false)
            }
        if (!isAtLeast(gradleVersionString, compatibility.runtime)) {
            throw GradleException(
                "$ERROR_STRING Java $javaRuntimeMajor requires Gradle ${compatibility.runtime} or higher to run Gradle.\n" +
                    "You are using Gradle $gradleVersionString.",
            )
        }

        // --- Gradle version warning ---
        val gradleInRange = gradleVersion >= GradleVersion.version("8.0") && gradleVersion <= GradleVersion.version("8.14.2")
        if (!gradleInRange) {
            if (gradleVersion < GradleVersion.version("8.0")) {
                logger.warn(
                    "$WARN_STRING Current Gradle version: $gradleVersionString is lower than 8.0\n" +
                        "(KMP Build Logic is not tested with this version; " +
                        "recommendation is to use the latest available Gradle release).",
                )
            } else if (gradleVersion > GradleVersion.version("8.14.2")) {
                logger.warn("$WARN_STRING This code would probably work, but KMP Build Logic is currently tested only up to Gradle 8.14.2.")
            } else {
                logger.warn("$WARN_STRING Gradle $gradleVersionString is outside tested range (8.0–8.14.2) of KMP Build Logic.")
            }
        } else {
            logger.lifecycle("$MARK_STRING Gradle $gradleVersionString is within tested range of KMP Build Logic.")
        }

        // --- Toolchain support warning ---
        val toolchainSupported = compatibility.toolchain?.let { isAtLeast(gradleVersionString, it) } ?: true
        if (!toolchainSupported) {
            logger.warn("$WARN_STRING Toolchain support may be limited for Java $javaRuntimeMajor with Gradle $gradleVersionString.")
        }

        return JavaRuntimeGradleVersionInfo(javaRuntimeMajor, gradleVersion, gradleInRange && toolchainSupported)
    }

    /**
     * Function [checkOsOrWarn]:
     *
     * - Does the 'Compatibility check for Operating System Version and Gradle Version.'.
     *
     */
    fun checkOsOrWarn(): OSInfo {
        val osInfo = detectOsInfo()
        // Adding a system property for operating system name is done for the metadata verification plugin
        System.setProperty("operatingSystemName", osInfo.name)
        val prettyOsName = osInfo.name.replaceFirstChar { it.uppercase() }
        logger.lifecycle("\nCompatibility check for Operating System and Gradle.\n")
        logger.lifecycle("$MARK_STRING Detected Operating System: $prettyOsName ${osInfo.version} (${osInfo.arch})")
        if (!isSupported(osInfo)) {
            logger.warn(
                """
        ❌ Compatibility check did not pass all checks for Operating System:
           Supported combinations:
${
                    supportedOsCombinations.joinToString("\n") {
                        "- ${it.first} ${it.second} ${it.third}"
                    }
                        .prependIndent("           ")
                }
                """,
            )
        } else {
            logger.lifecycle(
                "$POS_STRING Compatibility check passed for Operating System ${osInfo.name} ${osInfo.version} (${osInfo.arch})",
            )
        }
        return osInfo
    }

    /**
     * Function [parseJavaMajor]:
     *
     * - Helper function to provide the main java version number based upon given full version.
     *
     */
    fun parseJavaMajor(version: String): Int =
        version.split('.').let {
            when {
                it[0] == "1" -> it[1].toInt() // 1.8 → 8
                else -> it[0].toInt() // 17.0.2 → 17
            }
        }

    // Java Runtime Matrix table information source comes from the 'user guide' of Gradle:
    // https://docs.gradle.org/current/userguide/compatibility.html
    data class Compatibility(
        val toolchain: String?,
        val runtime: String,
    )

    val javaRuntimeMatrix: Map<Int, Compatibility> =
        mapOf(
            8 to Compatibility(null, "2.0"),
            9 to Compatibility(null, "4.3"),
            10 to Compatibility(null, "4.7"),
            11 to Compatibility(null, "5.0"),
            12 to Compatibility(null, "5.4"),
            13 to Compatibility(null, "6.0"),
            14 to Compatibility(null, "6.3"),
            15 to Compatibility("6.7", "6.7"),
            16 to Compatibility("7.0", "7.0"),
            17 to Compatibility("7.3", "7.3"),
            18 to Compatibility("7.5", "7.5"),
            19 to Compatibility("7.6", "7.6"),
            20 to Compatibility("8.1", "8.3"),
            21 to Compatibility("8.4", "8.5"),
            22 to Compatibility("8.7", "8.8"),
            23 to Compatibility("8.10", "8.10"),
            24 to Compatibility("8.14", "8.14"),
            // Java 25+ currently has no official support.
        )

    /**
     * Function [isAtLeast]:
     *
     * - Helper function to check if a used version is in line with the minimum required version.
     *
     */
    fun isAtLeast(
        actual: String,
        required: String,
    ): Boolean {
        val a =
            if (actual.startsWith("Gradle")) {
                actual.substring(7).split('.').map { it.toIntOrNull() ?: 0 }
            } else {
                actual.split('.').map { it.toIntOrNull() ?: 0 }
            }
        val b = required.split('.').map { it.toIntOrNull() ?: 0 }
        return a.zip(b).any { (x, y) -> x > y } || a == b
    }

    /**
     * Function [gradleVersionToString]:
     *
     * - Helper function to change gradle version by removing prefix 'Gradle' from it
     */
    fun gradleVersionToString(gradleVersion: String): String =
        if (gradleVersion.startsWith("Gradle")) {
            gradleVersion.substring(7)
        } else {
            gradleVersion
        }

    /**
     * Data class [OSInfo]:
     *
     * - A data class to 'store' relevant operating system information
     */
    data class OSInfo(
        val name: String,
        val version: String,
        val arch: String,
        val result: Boolean,
    )

    /**
     * Data class [JavaRuntimeGradleVersionInfo]
     *
     * - A data class to 'store' and 'hold' relevant java runtime information
     */
    data class JavaRuntimeGradleVersionInfo(
        val javaRuntimeMajor: Int,
        val gradleVersion: GradleVersion,
        val result: Boolean,
    )

    /**
     * Variable [supportedOsCombinations]
     *
     * - A variable that holds all operating system combinations which are supported by Gradle
     */
    val supportedOsCombinations =
        listOf(
            Triple("ubuntu", "22", "amd64"),
            Triple("ubuntu", "16", "amd64"),
            Triple("ubuntu", "16", "aarch64"),
            Triple("windows", "10", "amd64"),
            Triple("mac", "12", "amd64"),
            Triple("mac", "12", "aarch64"),
            Triple("alpine", "3.20", "amd64"),
            Triple("centos", "9", "amd64"),
        )

    /**
     * Function [isSupported]:
     *
     * - Does a supported compatibility check of the Operating System Version.
     *
     */
    fun isSupported(osInfo: OSInfo): Boolean =
        supportedOsCombinations.any { (os, version, arch) ->
            osInfo.name.contains(os) &&
                osInfo.version.startsWith(version) &&
                osInfo.arch == arch
        }

    /**
     * Function [detectOsInfo]:
     *
     * - Reads the current Operating System Version and provides the details as return information.
     *
     */
    fun detectOsInfo(): OSInfo {
        val rawOsName = System.getProperty("os.name").lowercase(Locale.getDefault())
        val rawOsVersion = System.getProperty("os.version")
        val rawOsArch = System.getProperty("os.arch")

        val normalizedArch =
            when (rawOsArch) {
                "x86_64", "amd64" -> "amd64"
                "aarch64", "arm64" -> "aarch64"
                else -> rawOsArch
            }

        val (distro, version) =
            when {
                rawOsName.contains("mac") -> "mac" to macOsMajorVersion(rawOsVersion)
                rawOsName.contains("windows") -> "windows" to windowsMajorVersion(rawOsVersion)
                rawOsName.contains("linux") -> parseEtcOsRelease()
                else -> rawOsName to rawOsVersion
            }
        return OSInfo(distro, version, normalizedArch, false)
    }

    /**
     * Function [parseEtcOsRelease]:
     *
     * - Reads the data of the current Operating System Version of Linux and provides the details of it as return information.
     *
     */
    fun parseEtcOsRelease(): Pair<String, String> {
        val file = File("/etc/os-release")
        if (!file.exists()) return "linux" to System.getProperty("os.version")

        val lines = file.readLines()
        val props =
            lines
                .mapNotNull {
                    val parts = it.split("=")
                    if (parts.size == 2) parts[0] to parts[1].trim('"') else null
                }.toMap()

        val id = props["ID"] ?: "linux"
        val versionId = props["VERSION_ID"] ?: "unknown"
        return id.lowercase(Locale.getDefault()) to versionId
    }

    /**
     * Function [macOsMajorVersion]:
     *
     * - Helper function to provide the main macOS version number based upon given full version.
     *
     */
    fun macOsMajorVersion(version: String): String = version.split(".").firstOrNull() ?: version

    /**
     * Function [windowsMajorVersion]:
     *
     * - Helper function to provide the main Windows version number based upon given full version.
     *
     */
    fun windowsMajorVersion(version: String): String = version.split(".").firstOrNull() ?: version
}

logger.info("Step 14: Creation of the environment compatibility check convention plugin.")

abstract class KmpBuildLogicEnvironmentCompatibilityCheckConventionPlugin : Plugin<Settings> {
    private val logger: Logger = Logging.getLogger(javaClass)
    private val posResultString = EnvironmentCompatibilityChecker.POS_RESULT_STRING
    private val warnResultString = EnvironmentCompatibilityChecker.WARN_RESULT_STRING

    override fun apply(target: Settings) {
        val result = EnvironmentCompatibilityChecker.run(target)
        if (result) {
            logger.lifecycle("$posResultString All compatibility checks passed\n")
        } else {
            logger.lifecycle("$warnResultString Not all compatibility checks were successful!\n")
        }
    }
}

logger.info("Step 15: Creation of toolchain checker.")

class ToolchainChecker {
    private val logger: Logger = Logging.getLogger(javaClass)
    private val markString = EnvironmentCompatibilityChecker.MARK_STRING
    private val settingString = EnvironmentCompatibilityChecker.SETTING_STRING

    fun run(settings: Settings): Boolean {
        logger.lifecycle("\nFoojay toolchain resolver check.\n")
        val gradleVersion: GradleVersion = GradleVersion.version(settings.gradle.gradleVersion)
        val minVersionWithBuildInFoojay: GradleVersion = GradleVersion.version("8.4")
        val foojayPluginNeeded = gradleVersion < minVersionWithBuildInFoojay
        System.setProperty("foojayPluginNeeded", foojayPluginNeeded.toString())
        if (foojayPluginNeeded) {
            logger.lifecycle(
                "$settingString Foojay toolchain resolver plugin is needed, because current Gradle version ($gradleVersion) is < '8.4'.",
            )
        } else {
            logger.lifecycle("$markString Foojay toolchain resolver plugin is been added by Gradle itself since version '8.4'.")
        }
        return !foojayPluginNeeded
    }
}

logger.info("Step 16: Toolchain plugin check creation.")

abstract class KmpBuildLogicToolchainCheckConventionPlugin : Plugin<Settings> {
    private val logger: Logger = Logging.getLogger(javaClass)
    private val posResultString = EnvironmentCompatibilityChecker.POS_RESULT_STRING
    private val warnResultString = EnvironmentCompatibilityChecker.WARN_RESULT_STRING

    override fun apply(settings: Settings) {
        val result = ToolchainChecker().run(settings)
        if (result) {
            logger.lifecycle("\n")
            logger.lifecycle("$posResultString Foojay toolchain resolver check passed\n")
        } else {
            logger.lifecycle("\n")
            logger.lifecycle("$warnResultString Not all Foojay toolchain resolver checks were successful!\n")
        }
    }
}

logger.info("Step 17: Library Selector plugin.")

abstract class KmpBuildLogicLibrarySelectorConventionPlugin : Plugin<Settings> {
    private val logger: Logger = Logging.getLogger(javaClass)
    private val markString = EnvironmentCompatibilityChecker.MARK_STRING
    private val posResultString = EnvironmentCompatibilityChecker.POS_RESULT_STRING
    private val versionProfile: String = System.getProperty("kmpbuildlogic.properties.version.profile")

    override fun apply(settings: Settings) {
        logger.lifecycle("\nLibrary version selector\n")
        val libraryVersion =
            when (versionProfile) {
                "alpha" -> "Alpha"
                "beta" -> "Beta"
                "rc" -> "Release Candidate"
                "ga" -> "General Availability"
                else -> "General Availability"
            }
        logger.lifecycle("$markString The versions library which will be used is: $libraryVersion Version.\n")
        logger.lifecycle("$posResultString Library selection succeeded")
        logger.info("\n")
        logger.info("    ==========                      End of settings.gradle.kts from setting-logic                      ==========\n")
    }
}

logger.info("Step 18: Jvm Toolchain plugin.")

/**
 * Jvm Toolchain convention plugin using System.setProperty to define JVM version.
 */
abstract class JvmToolchainConventionPlugin : Plugin<Settings> {
    private val logger = Logging.getLogger(javaClass)
    private val informString = EnvironmentCompatibilityChecker.INFORM_STRING
    private val informResultString = EnvironmentCompatibilityChecker.INFORM_RESULT_STRING
    private val markString = EnvironmentCompatibilityChecker.MARK_STRING
    private val posResultString = EnvironmentCompatibilityChecker.POS_RESULT_STRING

    override fun apply(settings: Settings) {
        logger.lifecycle("JVM version setter.\n")

        // Enable automatically detection of available JDK's on the system
        val jdkDetectionVersionKey = "org.gradle.java.installations.auto-detect"
        val autoDetectProperty = System.getProperty(jdkDetectionVersionKey)
        if (autoDetectProperty == null) {
            // Only set if not already set
            System.setProperty(jdkDetectionVersionKey, "true")
            logger.lifecycle("$markString Set '$jdkDetectionVersionKey' to 'true'")
        } else {
            logger.lifecycle("$informString $jdkDetectionVersionKey already set to: $autoDetectProperty, leaving as is")
        }
        val jvmVersionKey = "org.gradle.jvm.version"
        val defaultJvmVersion = "21"
        val jvmDetectProperty = System.getProperty(jvmVersionKey)
        if (jvmDetectProperty == null) {
            System.setProperty(jvmVersionKey, defaultJvmVersion)
            logger.lifecycle("$markString Set system property: '$jvmVersionKey' to '$defaultJvmVersion'\n")
        } else {
            logger.lifecycle("$informResultString $jvmVersionKey already set to: $jvmDetectProperty, leaving as is\n")
        }
        logger.lifecycle("$posResultString JVM version is set.\n")
    }
}

logger.info("Step 19: Metadata Verification")

/**
 * A Settings plugin which triggers Gradle dependency verification metadata generation,
 * based on changes in version catalog TOML files.
 */
abstract class MetadataVerificationSettingsPlugin : Plugin<Settings> {
    private val markString = EnvironmentCompatibilityChecker.MARK_STRING
    private val posResultStringWithReturn = EnvironmentCompatibilityChecker.POS_RESULT_STRING_WITH_RETURN
    private val processString = EnvironmentCompatibilityChecker.PROCESS_STRING
    private val warnResultStringWithReturn = EnvironmentCompatibilityChecker.WARN_STRING_WITH_RETURN

    companion object {
        private var hasRun = false
    }

    private val logger = Logging.getLogger(javaClass)

    private var result = true

    val catalogFiles =
        mapOf(
            "libs.versions.toml" to "ga",
            "alpha.versions.toml" to "alpha",
            "beta.versions.toml" to "beta",
            "rc.versions.toml" to "rc",
        )

    override fun apply(settings: Settings) {
        logger.lifecycle("\nMetadata verification\n")
        if (hasRun) {
            logger.lifecycle("Metadata verification already executed; skipping.")
            return
        }
        hasRun = true

        val root = settings.rootDir
        val verificationFile = root.resolve("../gradle/verification-metadata.xml")

        if (!verificationFile.exists()) {
            logger.lifecycle("$processString Creating missing file $verificationFile")
            verificationFile.parentFile.mkdirs()
            verificationFile.writeText("<verification-metadata></verification-metadata>")
            logger.lifecycle("\u001B[F     $markString Created verification-metadata.xml")
        } else {
            logger.lifecycle("$markString Verification metadata file exists")
        }
        val verificationTime = verificationFile.lastModified()

        // First date check is done on the gradle wrapper file
        val gradleWrapper = root.resolve("../gradle/wrapper/gradle-wrapper.properties")
        val gradleWrapperTime = gradleWrapper.lastModified()
        val gradleNeeds = gradleWrapperTime > verificationTime
        if (gradleNeeds) {
            logger.lifecycle(
                "$processString Processing is needed for all version catalogs (gradle wrapper changed since last verification)",
            )
        }

        for ((catalogName) in catalogFiles) {
            val toml = root.resolve("../gradle/$catalogName")
            val tomlTime = toml.lastModified()
            val needs = tomlTime > verificationTime

            if (needs || gradleNeeds) {
                if (gradleNeeds) {
                    logger.lifecycle("$processString Processing is needed $catalogName")
                } else {
                    logger.lifecycle("$processString Processing is needed $catalogName (changed since last verification)")
                }
                result = false
            } else {
                logger.lifecycle("$markString No need to process $catalogName")
            }
        }
        if (result) {
            logger.lifecycle("$posResultStringWithReturn Metadata verification check passed")
        } else {
            logger.lifecycle(
                "$warnResultStringWithReturn Not all Metadata verifications were successful! " +
                    "There is a need to run the task 'verifyAllVersionsCatalogs'.",
            )
        }
    }
}

logger.info("Step 20: Enabler for running the checkers and selector.")
gradle.settingsEvaluated {
    apply<KmpBuildLogicEnvironmentCompatibilityCheckConventionPlugin>()
    apply<JvmToolchainConventionPlugin>()
    apply<KmpBuildLogicToolchainCheckConventionPlugin>()
    apply<MetadataVerificationSettingsPlugin>()
    apply<KmpBuildLogicLibrarySelectorConventionPlugin>()
}
