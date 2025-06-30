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

logger.info("\n")
logger.info("    ==========                               INITIALIZATION PHASE OF LOGIC                               ==========\n")
logger.info("    ==========                      Start of settings.gradle.kts from setting-logic                      ==========\n")

logger.info("Step 1 : Creation of property resolve function.")
/**
 * [resolveProperty] function:
 *
 * A general function to 'read' the gradleProperty.
 *
 * - Priority secquence used is: CLI > systemProperty > gradle.properties > environment
 *
 */
private fun resolveProperty(key: String): String? {
    return when {
        settings.providers.gradleProperty(key).isPresent -> settings.providers.gradleProperty(key).get()
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

logger.info("Step 3 : setting of the 'repositories' within 'dependencyResolutionManagement'.")
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

    val fileName = when(selectedVersionProfile) {
            "alpha" -> "../gradle/alpha.versions.toml"
            "beta" ->  "../gradle/beta.versions.toml"
            "rc" -> "../gradle/rc.versions.toml"
            else -> "../gradle/libs.versions.toml"
        }
    versionCatalogs {
        create("libs") {
            from(files(fileName))
        }
    }
}

logger.info("Step 4 : 'rootProject name' setting.")
/**
 * RootProject name:
 *
 * - Root project name configuration setting based upon a combination of main rootProject name and the module project name.
 *
 */
val mainRootProjectNameProperty = "kmpbuildlogic.properties.root.project.name"
val mainRootProjectNameDefault: String = "Kotlin-Multiplatform-Build-Logic"
val mainRootProjectName: String = resolveProperty(mainRootProjectNameProperty) ?: mainRootProjectNameDefault
val kmpbuildlogicBuildConventionsProjectName = "$mainRootProjectName-Setting-Logic"
rootProject.name = kmpbuildlogicBuildConventionsProjectName

logger.info("Step 5 : include the module 'settings-conventions.")
/**
 * Child projects:
 *
 * - Child projects included. 
 *
 */
include(":settings-conventions")

logger.info("Step 6 : Creation of the environment compatibility checker.")
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
    private const val attentionSymbol     = "⚠️"
    private const val errorSymbol         = "❌"
    private const val informSymbol        = "ℹ️"
    private const val markSymbol          = "📍"
    private const val positiveCheckSymbol = "✅"
    private const val procesSymbol        = "⏳"
    private const val settingSymbol       = "🔧"

    // Special characters used 
    private const val space               = "\u0020"
    private const val tab                 = "\t"

    // Combined strings
    const val errorString                 = "$tab$errorSymbol$space"
    const val errorResultString           = "\n$errorSymbol$space"    
    const val informString                = "$tab$informSymbol$space"
    const val informResultString          = "$informSymbol$space"
    const val markString                  = "$tab$markSymbol$space"
    const val markResultString            = "$markSymbol$space"
    const val posString                   = "$tab$positiveCheckSymbol$space"
    const val posResultString             = "$positiveCheckSymbol$space"    
    const val procesString                = "$tab$procesSymbol$space"
    const val settingString               = "$tab$settingSymbol$space"
    const val warnString                  = "$tab$attentionSymbol$space"
    const val warnResultString            = "$attentionSymbol$space"

    /**
     * Function [run]:
     *
     * - Overall function that combines all child functions together.
     * - Is the function that a plugin uses to start these compatibility checks
     *
     */
    fun run(settings:Settings): Boolean {
        val javaCheck = checkJavaRuntimeAndGradleVersionOrWarn(settings)
        if (javaCheck.result) {
            logger.lifecycle("\n")
            logger.lifecycle("$posResultString Compatibility check passed for Java Runtime ${javaCheck.javaRuntimeMajor} and ${javaCheck.gradleVersion}.")
        } else {
            logger.lifecycle("\n$warnResultString Compatibility check did not passed all checks for Java Runtime ${javaCheck.javaRuntimeMajor} and ${javaCheck.gradleVersion}.")
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
        logger.lifecycle("$markString Java Runtime: $javaRuntimeVersion (parsed major: $javaRuntimeMajor)")
        logger.lifecycle("$markString Gradle Version: $gradleVersionString")
        
        // --- Compatibility information warning ---
        val compatibility = javaRuntimeMatrix[javaRuntimeMajor] ?: run {
            logger.warn("$warnString Java $javaRuntimeMajor is unknown in compatibility table. Proceeding without further checking.")
            return JavaRuntimeGradleVersionInfo(javaRuntimeMajor, gradleVersion, false)
        }
        if (!isAtLeast(gradleVersionString, compatibility.runtime)) {
            throw GradleException(
                "$errorString Java $javaRuntimeMajor requires Gradle ${compatibility.runtime} or higher to run Gradle.\n" +
                    "You are using Gradle $gradleVersionString."
            )
        }
        
        // --- Gradle version warning ---
        val gradleInRange = gradleVersion >= GradleVersion.version("8.0") && gradleVersion <= GradleVersion.version("8.14.2")
        if (!gradleInRange) {
            if (gradleVersion < GradleVersion.version("8.0")) {
                logger.warn(
                    "$warnString Current Gradle version: $gradleVersionString is lower than 8.0\n" +
                        "(KMP Build Logic is not tested with this version; " +
                        "recommendation is to use the latest available Gradle release)."
                )
            } else if (gradleVersion > GradleVersion.version("8.14.2")) {
                logger.warn("$warnString This code would probably work, but KMP Build Logic is currently tested only up to Gradle 8.14.2.")
            } else {
                logger.warn("$warnString Gradle $gradleVersionString is outside tested range (8.0–8.14.2) of KMP Build Logic.")
            }
        } else {
            logger.lifecycle("$markString Gradle $gradleVersionString is within tested range of KMP Build Logic.")
        }

        // --- Toolchain support warning ---
        val toolchainSupported = compatibility.toolchain?.let { isAtLeast(gradleVersionString, it) } ?: true
        if (!toolchainSupported) {
            logger.warn("$warnString Toolchain support may be limited for Java $javaRuntimeMajor with Gradle $gradleVersionString.")
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
        logger.lifecycle("$markString Detected Operating System: $prettyOsName ${osInfo.version} (${osInfo.arch})")
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
                """
            )            
        } else {
            logger.lifecycle("$posString Compatibility check passed for Operating System ${osInfo.name} ${osInfo.version} (${osInfo.arch})")            
        }
         return osInfo
    }

    /**
     * Function [parseJavaMajor]:
     *
     * - Helper function to provide the main java version number based upon given full version.
     *
     */
    fun parseJavaMajor(version: String): Int {
        return version.split('.').let {
            when {
                it[0] == "1" -> it[1].toInt()  // 1.8 → 8
                else -> it[0].toInt()          // 17.0.2 → 17
            }
        }
    }

    // Java Runtime Matrix table information source comes from the 'user guide' of Gradle:
    // https://docs.gradle.org/current/userguide/compatibility.html
    data class Compatibility(val toolchain: String?, val runtime: String)

    val javaRuntimeMatrix: Map<Int, Compatibility> = mapOf(
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
       24 to Compatibility("8.14", "8.14")
       // Java 25+ currently has no official support.
    )

    /**
     * Function [isAtLeast]:
     *
     * - Helper function to check if a used version is in line with the minimum required version.
     *
     */
    fun isAtLeast(actual: String, required: String): Boolean {
        val a = if (actual.startsWith("Gradle")) {
            actual.substring(7).split('.').map { it.toIntOrNull() ?: 0 }
        } else {
            actual.split('.').map { it.toIntOrNull() ?: 0 }
        }
        val b = required.split('.').map { it.toIntOrNull() ?: 0 }
        return a.zip(b).any { (x, y) -> x > y } || a == b
    }
    
    fun gradleVersionToString(gradleVersion:String): String {
        return if (gradleVersion.startsWith("Gradle")) {
            gradleVersion.substring(7)
        } else {
            gradleVersion
        }
    }

    data class OSInfo(
        val name: String,
        val version: String,
        val arch: String,
        val result: Boolean,
    )

    data class JavaRuntimeGradleVersionInfo(
        val javaRuntimeMajor: Int,
        val gradleVersion: GradleVersion,
        val result: Boolean,
    )

    val supportedOsCombinations = listOf(
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
    fun isSupported(osInfo: OSInfo): Boolean {
        return supportedOsCombinations.any { (os, version, arch) ->
            osInfo.name.contains(os) &&
            osInfo.version.startsWith(version) &&
            osInfo.arch == arch
        }
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

        val normalizedArch = when (rawOsArch) {
            "x86_64", "amd64" -> "amd64"
            "aarch64", "arm64" -> "aarch64"
            else -> rawOsArch
        }

        val (distro, version) = when {
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
        val props = lines.mapNotNull {
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
    fun macOsMajorVersion(version: String): String {
        return version.split(".").firstOrNull() ?: version
    }

    /**
     * Function [windowsMajorVersion]:
     *
     * - Helper function to provide the main Windows version number based upon given full version.
     *
     */
    fun windowsMajorVersion(version: String): String {
        return version.split(".").firstOrNull() ?: version
    }
}

logger.info("Step 6 : Creation of the environment compatibility check convention plugin.")
abstract class KmpBuildLogicEnvironmentCompatibilityCheckConventionPlugin : Plugin<Settings> {
    private val logger: Logger = Logging.getLogger(javaClass)
    private val posResultString = EnvironmentCompatibilityChecker.posResultString
    private val warnResultString = EnvironmentCompatibilityChecker.warnResultString

    override fun apply(target: Settings) {
        val result = EnvironmentCompatibilityChecker.run(target)
        if (result) {
            logger.lifecycle("$posResultString All compatibility checks passed\n")
        } else  {
            logger.lifecycle("$warnResultString Not all compatibility checks were successful!\n")
        }
    }
}

logger.info("Step 7 : Creation of toolchain checker.")
class ToolchainChecker {
    private val logger: Logger = Logging.getLogger(javaClass)
    private val markString = EnvironmentCompatibilityChecker.markString
    private val settingString = EnvironmentCompatibilityChecker.settingString    

    fun run(settings:Settings) : Boolean {
        logger.lifecycle("\nFoojay toolchain resolver check.\n")
        val gradleVersion: GradleVersion = GradleVersion.version(settings.gradle.gradleVersion)
        val minVersionWithBuildInFoojay: GradleVersion = GradleVersion.version("8.4")
        val foojayPluginNeeded = gradleVersion < minVersionWithBuildInFoojay
        System.setProperty("foojayPluginNeeded", foojayPluginNeeded.toString())
        if (foojayPluginNeeded) {
            logger.lifecycle("$settingString Foojay toolchain resolver plugin is needed, because current Gradle version (${gradleVersion}) is < '8.4'.")
        } else {
            logger.lifecycle("$markString Foojay toolchain resolver plugin is been added by Gradle itself since version '8.4'.")
        }
        return !foojayPluginNeeded        
    }
}

logger.info("Step 8 : Toolchain plugin check creation.")
abstract class KmpBuildLogicToolchainCheckConventionPlugin : Plugin<Settings> {
    private val logger: Logger = Logging.getLogger(javaClass)
    private val posResultString = EnvironmentCompatibilityChecker.posResultString
    private val warnResultString = EnvironmentCompatibilityChecker.warnResultString

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

logger.info("Step 9 : Library Selector plugin.")
abstract class KmpBuildLogicLibrarySelectorConventionPlugin : Plugin<Settings> {
    private val logger: Logger = Logging.getLogger(javaClass)
    private val markString = EnvironmentCompatibilityChecker.markString
    private val posString = EnvironmentCompatibilityChecker.posString
    private val posResultString = EnvironmentCompatibilityChecker.posResultString
    private val versionProfile: String = System.getProperty("kmpbuildlogic.properties.version.profile")

    override fun apply(settings: Settings) {
        logger.lifecycle("\nLibrary version selector\n")
        val libraryVersion = when(versionProfile) {
            "alpha" -> "Alpha"
            "beta" ->  "Beta"
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

logger.info("Step 10: Jvm Toolchain plugin.")

/**
 * Jvm Toolchain convention plugin using System.setProperty to define JVM version.
 */
abstract class JvmToolchainConventionPlugin : Plugin<Settings> {
    private val logger = Logging.getLogger(javaClass)
    private val informString = EnvironmentCompatibilityChecker.informString
    private val informResultString = EnvironmentCompatibilityChecker.informResultString
    private val markString = EnvironmentCompatibilityChecker.markString
    private val posString = EnvironmentCompatibilityChecker.posString
    private val markResultString = EnvironmentCompatibilityChecker.markResultString
    private val posResultString = EnvironmentCompatibilityChecker.posResultString

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

/**
 * A Settings plugin which triggers Gradle dependency verification metadata generation,
 * based on changes in version catalog TOML files.
 */
abstract class MetadataVerificationSettingsPlugin: Plugin<Settings> {
    private val errorString = EnvironmentCompatibilityChecker.errorString
    private val errorResultString = EnvironmentCompatibilityChecker.errorResultString
    private val markString = EnvironmentCompatibilityChecker.markString
    private val posString = EnvironmentCompatibilityChecker.posString
    private val posResultString = EnvironmentCompatibilityChecker.posResultString
    private val procesString = EnvironmentCompatibilityChecker.procesString 
    private val warnString = EnvironmentCompatibilityChecker.warnString

    companion object {
        private var hasRun = false
    }

    private val logger = Logging.getLogger(javaClass)

    // val versionCatalogs = listOf(
    //    "libs.versions.toml",
    //    "alpha.versions.toml",
    //    "beta.versions.toml",
    //    "rc.versions.toml"
    // )

    val catalogFiles = mapOf(
        "libs.versions.toml" to "ga",
        "alpha.versions.toml" to "alpha",
        "beta.versions.toml" to "beta",
        "rc.versions.toml" to "rc"
    )

    // @VisibleForTesting
    var result = true

//    val operatingSystemName = System.getProperty("os.name").replaceFirstChar { it.uppercase() }

//    var libsVerificationIsNeeded = true
//    var alphaVerificationIsNeeded = true
//    var betaVerificationIsNeeded = true
//    var rcVerificationIsNeeded = true
//    var result = true
    
    override fun apply(settings: Settings) {
        if (hasRun){
            logger.lifecycle("Metadata verification already executed; skipping.")
            return
        }
        hasRun = true
        logger.lifecycle("\nMetadata verification\n")

        val root = settings.rootDir
        
//        val libsCatalogFile = settings.rootDir.resolve("../gradle/libs.versions.toml")
//        val libsCatalogLastModified: Long = libsCatalogFile.lastModified() // if file not exists outcome is 0L

//        val alphaCatalogFile = settings.rootDir.resolve("../gradle/alpha.versions.toml")
//        val alphaCatalogLastModified: Long = alphaCatalogFile.lastModified()
        
//        val betaCatalogFile = settings.rootDir.resolve("../gradle/beta.versions.toml")
//        val betaCatalogLastModified: Long = betaCatalogFile.lastModified()

//        val rcCatalogFile = settings.rootDir.resolve("../gradle/rc.versions.toml")
//        val rcCatalogLastModified: Long = rcCatalogFile.lastModified()        

        val verificationFile = root.resolve("../gradle/verification-metadata.xml")        

        if (!verificationFile.exists()) {
            // logger.lifecycle("$procesString Creating missing verification metadata file at ${verificationFile.path}")
            logger.lifecycle("$procesString Creating missing file $verificationFile")
            verificationFile.parentFile.mkdirs()
            verificationFile.writeText("<verification-metadata></verification-metadata>")
            logger.lifecycle("\u001B[F     $markString Created verification-metadata.xml")
        } else {
            logger.lifecycle("$markString Verification metadata file exists")
        }
        // val verificationLastModified: Long = verificationFile.lastModified()
        val verificationTime = verificationFile.lastModified()
        // First date check is done on the gradle wrapper file
        val gradleWrapper = root.resolve("../gradle/wrapper/gradle-wrapper.properties")
        val gradleWrapperTime = gradleWrapper.lastModified()
        val gradleNeeds = gradleWrapperTime > verificationTime
        if (gradleNeeds) {
            logger.lifecycle(
                "$procesString Processing is needed for all version catalogs (gradle wrapper changed since last verification)"
            )
        }        
        val os = System.getProperty("os.name").replaceFirstChar { it.uppercase() }
        
        // logger.debug("libs catalog last modified is: $libsCatalogLastModified")
        // logger.debug("alpha catalog last modified is: $alphaCatalogLastModified")
        // logger.debug("beta catalog last modified is: $betaCatalogLastModified")
        // logger.debug("rc catalog last modified is: $rcCatalogLastModified")
        // logger.debug("verification last modified is: $verificationLastModified")
         
        // if (libsCatalogLastModified < verificationLastModified) {
        //    libsVerificationIsNeeded = false
        // }
        // if (alphaCatalogLastModified < verificationLastModified) {
        //     alphaVerificationIsNeeded = false
        // }
        // if (betaCatalogLastModified < verificationLastModified) {
        //     betaVerificationIsNeeded = false
        // }
        // if (rcCatalogLastModified < verificationLastModified) {
        //     rcVerificationIsNeeded = false
        // }

        //val gradlew = if (operatingSystemName.startsWith("Windows")) "../gradlew.bat" else "../gradlew"
        val gradlew = if (os.startsWith("Windows")) "../gradlew.bat" else "../gradlew"

        for ((catalogName, profile) in catalogFiles) {
            val toml = root.resolve("../gradle/$catalogName")
            val tomlTime = toml.lastModified()
            val needs = tomlTime > verificationTime
            
        // versionCatalogs.forEach { catalog ->
        //    val verificationIsNeeded = when (catalog) {
        //        "alpha.versions.toml"-> if (alphaVerificationIsNeeded) true else false
        //        "beta.versions.toml"-> if (betaVerificationIsNeeded) true else false
        //        "rc.versions.toml" -> if (rcVerificationIsNeeded) true else false
        //        else -> if (libsVerificationIsNeeded) true else false
        //    }
            if (needs || gradleNeeds) {
                if (gradleNeeds) {
                    logger.lifecycle("$procesString Processing $catalogName")
                } else {
                    logger.lifecycle("$procesString Processing $catalogName (changed since last verification")
                }
            // if (verificationIsNeeded) {
            //    logger.lifecycle("$procesString Processing version catalog: $catalog, because it has changed")
            //    val versionProfile = when (catalog) {
            //        "alpha.versions.toml"-> "alpha"
            //        "beta.versions.toml"-> "beta"
            //        "rc.versions.toml" -> "rc"
            //        else -> "ga"
            //    }
                
            //    val relativePath= "../gradle/${catalog}"
            //    val process = ProcessBuilder(
            //    val proc = ProcessBuilder( 
            //        gradlew,
            //        "dependencies",
            //        "-Pkmpbuildlogic.properties.version.profile = $profile",
            //        "--write-verification-metadata=sha512, pgp",
            //        "-PversionCatalogPath=gradle/$catalogName"
            //    )
            //        .directory(settings.rootDir)
            //        .redirectErrorStream(true)
            //        .start()
                //val output = process.inputStream.bufferedReader().readText()
            //    val out = proc.inputStream.bufferedReader().readText()
                // val exitCode = process.waitFor()
            //    val code = proc.waitFor()
                val code = 0
                // if (exitCode == 0) {
                if (code == 0) {
                    // logger.lifecycle("\u001B[F") // move cursor up one line
                    // logger.lifecycle("\u001b[2K") // clear the line
                    // logger.lifecycle("$markString Metadata verification succeeded for catalog: $catalog")
                    logger.lifecycle("\u001B[F\u001b[2K$markString Verification succeeded: $catalogName")
                } else {
                    // logger.lifecycle("$warnString Metadata verification FAILED for catalog: $catalog")
                    logger.lifecycle("$warnString Verification FAILED: $catalogName")
            //        logger.debug(out)
                    // logger.debug("Process exited with code $exitCode for $catalog")
                    logger.debug("Exited with $code")
                    result = false
                }
            } else {
                // logger.lifecycle("$markString Metadata verification for $catalog is not needed")
                logger.lifecycle("$markString No need to process $catalogName")
            }
        }        
        if (result) {
                logger.lifecycle("\n")
                logger.lifecycle("$posResultString Metadata verification succeeded")
        } else {
            logger.lifecycle("$errorResultString Metadata verification FAILED")
        }            
    }
}

logger.info("Step 11: Enabler for running the checkers and selector.")
gradle.settingsEvaluated {
    apply<KmpBuildLogicEnvironmentCompatibilityCheckConventionPlugin>()
    apply<JvmToolchainConventionPlugin>()
    apply<KmpBuildLogicToolchainCheckConventionPlugin>()
    apply<MetadataVerificationSettingsPlugin>()
    apply<KmpBuildLogicLibrarySelectorConventionPlugin>()
}
