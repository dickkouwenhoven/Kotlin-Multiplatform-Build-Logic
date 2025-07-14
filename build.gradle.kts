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
            val apacheCommonsCompress = providers.gradleProperty("apache.commons.compress.version").get().toString()
            val apacheCommonsIo = providers.gradleProperty("apache.commons.io.version").get().toString()

            // Apache Commons libraries
            if (requested.group == "org.apache.commons" && requested.name == "commons-compress") {
                useVersion(apacheCommonsCompress)
                because("CVE-2024-26308, CVE-2024-25710, CVE-2023-42503")
            }
            if (requested.group == "commons-io" && requested.name == "commons-io") {
                useVersion(apacheCommonsIo)
                because("CVE-2024-26308, CVE-2023-42503")
            }

            val fasterxmlJacksonCore = providers.gradleProperty("fasterxml.jackson.core.version").get().toString()
            val fasterxmlWoodstoxCore = providers.gradleProperty("fasterxml.woodstox.core.version").get().toString()

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

    alias(libs.plugins.gradlePluginPublish) apply false
    alias(libs.plugins.gradleupShadowPlugin) apply false
    alias(libs.plugins.githubGmazzoBuildconfig) apply false
    alias(libs.plugins.jetbrainsKotlinPluginSerialization) apply false
    alias(libs.plugins.kordampGradlePortalPluginPomChecker) apply false
    java
    `jvm-toolchains`
    id("kmpbuildlogic.verification.convention.plugin")
    id("dokkatoo.convention.plugin") apply false
    id("jvm.toolchain.evaluate.convention.plugin") apply false
    id("kmpbuildlogic.project.convention.plugin") apply false
}

logger.info("Step 4 : dependencies")
dependencies {
    implementation(gradleApi())
}

logger.info("Step 5 : define publishing version variables")
// Get default snapshot from JVM properties
private val defaultSnapshotVersion: String = System.getProperty("kmpbuildlogic.properties.default.snapshot.version") ?: "0.0.1-SNAPSHOT"
// Ensure deploy.version and build.number are consistent
findProperty("kmpbuildlogic.properties.deploy.version")?.let {
    require(findProperty("kmpbuildlogic.properties.build.number") != null) {
        "'kmpbuildlogic.properties.build.number' parameter must be defined when 'deploy.version' is used."
    }
}
// Define buildNumber and version
val buildNumber by extra(
    findProperty("kmpbuildlogic.properties.build.number")?.toString() ?: defaultSnapshotVersion,
)
// Tries to use an explicit deploy.version; otherwise uses buildNumber.
val kmpBuildLogicVersion by extra(
    findProperty("kmpbuildlogic.properties.deploy.version")?.toString()?.let { deploySnapshotStr ->
        if (deploySnapshotStr != "default.snapshot") deploySnapshotStr else defaultSnapshotVersion
    } ?: buildNumber,
)
allprojects {
    extra["buildNumber"] = rootProject.extra["buildNumber"]!!
    extra["kmpBuildLogicVersion"] = rootProject.extra["kmpBuildLogicVersion"]!!
}

logger.info("Step 6 : define task groups variables")
val kmpBuildLogicBuildGroup = "KMP Build Logic Build"
val kmpBuildLogicCheckGroup = "KMP Build Logic Check"
val kmpBuildLogicDependenciesGroup = "KMP Build Logic Dependency"
val kmpBuildLogicDevelocityGroup = "KMP Build Logic Develocity"
val kmpBuildLogicDocumentationGroup = "KMP Build Logic Documentation"
val kmpBuildLogicHelpGroup = "KMP Build Logic Help"
val kmpBuildLogicPublishPluginGroup = "KMP Build Logic Publish Plugin"
val kmpBuildLogicSetupGroup = "KMP Build Logic Setup"
val kmpBuildLogicTestGroup = "KMP Build Logic Test"
val kmpBuildLogicVerificationGroup = "KMP Build Logic Verification"

logger.info("Step 7 : define file directory structure")
// Centralized build directory
val commonBuildDir = File(rootDir, "build")
// Distribution root
val distDir by extra("$rootDir/dist")
// Where build logic artifacts reside
val distKmpBuildLogicHomeDir by extra("$distDir/kmpBuildLogic")
// Subfolder under kmpbuildlogic for libs
val distLibDir = "$distKmpBuildLogicHomeDir/lib"
// local machine-specific data
val commonLocalDataDir = "$rootDir/local"
// Built artifacts for publishing
val artifactsDir = "$distDir/artifacts"
// Local maven repository path from a gradle property
val localRepositoryDir = providers.gradleProperty("kmpbuildlogic.properties.local.repository.path").orNull

allprojects {
    extra["commonBuildDir"] = project.file(commonBuildDir)
    extra["distLibDir"] = project.file(distLibDir)
    extra["commonLocalDataDir"] = project.file(commonLocalDataDir)
    extra["artifactsDir"] = project.file(artifactsDir)
    extra["localRepositoryDir"] = localRepositoryDir?.let { File(it) }
}

logger.info("Step 8 : adding dependencies for ktlint")
dependencies {
    ktlint(libs.pinterestKtlintCli) {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

logger.info("Step 9 : creation of task 'ktlint check'")
tasks.register<JavaExec>("ktlintCheck") {
    group = kmpBuildLogicVerificationGroup
    description = "               - Check Kotlin code style."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args(
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}

logger.info("Step 10: create ktlint format task with settings")
tasks.register<JavaExec>("ktlintFormat") {
    group = kmpBuildLogicVerificationGroup
    description = "              - Check Kotlin code style and format."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    args(
        "-F",
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}

logger.info("Step 11: add ktlint check to default gradle task check activities")
tasks.named("check") {
    dependsOn("ktlintCheck")
    dependsOn("test")
}

logger.info("Step 12: registering task 'printFullyQualifiedTasks")
tasks.register("printFullyQualifiedTasks") {
    group = "help"
    description = "  - Prints all tasks with their fully qualified path."
    doLast {
        allprojects.forEach { project ->
            project.tasks.forEach { task ->
                println("${project.path}:${task.name}")
            }
        }
    }
}

logger.info("Step 13: task implementation of 'listPlugins'")

abstract class ListPluginsTask : DefaultTask() {
    @get:Internal
    abstract val pluginNames: ListProperty<String>

    init {
        pluginNames.set(project.plugins.map { it.javaClass.name })
    }

    @TaskAction
    fun listPlugins() {
        println("Applied plugins:\n")
        pluginNames.get().forEach { println(" = $it") }
    }
}

logger.info("Step 14: task registration of 'listPlugins'")
tasks.register<ListPluginsTask>("listAllPlugins") {
    group = kmpBuildLogicSetupGroup
    description = "            - Show all applied plugins in this project."
}

logger.info("Step 15: task registration of 'listAllTasks")
tasks.register<TaskReportTask>("listAllTasks") {
    group = kmpBuildLogicSetupGroup
    description = "              - Show all tasks created within this project."
    setShowDetail(true)
}

logger.info("Step 16: task registration of 'cleanupArtifacts'")
tasks.register<Delete>("cleanupArtifacts") {
    group = kmpBuildLogicBuildGroup
    description = "          - Cleans the artifact directory."
    delete = setOf(artifactsDir)
}

logger.info("Step 17: task registration of 'dependenciesAll")
tasks.register("dependenciesAll") {
    group = kmpBuildLogicDependenciesGroup
    description = "           - Shows all dependencies."
    subprojects.forEach {
        dependsOn(it.tasks.named("dependencies"))
    }
}

val functionalTests =
    listOf(
        "publish:gradlePlugin",
        "setting-logic:settings-conventions",
        "build-logic:build-conventions",
    )
val integrationTests =
    listOf(
        "publish:gradlePlugin",
        "setting-logic:settings-conventions",
        "build-logic:build-conventions",
    )
val unitTests =
    listOf(
        "publish:gradlePlugin",
        "setting-logic:settings-conventions",
        "build-logic:build-conventions",
    )

logger.info("Step 18: task registration of 'functionalTestAll'")
tasks.register("functionalTestAll") {
    group = kmpBuildLogicTestGroup
    description = "        - Does all defined functional tests"
    functionalTests.forEach {
        dependsOn("$it:check")
    }
}

logger.info("Step 19: task registration of 'integrationTestAll'")
tasks.register("integrationTestAll") {
    group = kmpBuildLogicTestGroup
    description = "         - Does all defined integration tests"
    integrationTests.forEach {
        dependsOn("$it:check")
    }
}

logger.info("Step 20: task registration of 'unitTestAll")
tasks.register("unitTestAll") {
    group = kmpBuildLogicTestGroup
    description = "               - Does all defined unit tests"
    unitTests.forEach {
        dependsOn("$it:check")
    }
}

logger.info("Step 21: creation of function 'getMavenWindowsCommand()'")

fun getMavenWindowsCommand(): List<String> =
    when {
        org.gradle.internal.os.OperatingSystem
            .current()
            .isWindows -> listOf("cmd", "/c", "mvnw.cmd")
        else -> listOf("./mnvw")
    }

logger.info("Step 22: creation of task 'mavenInstall'")
tasks.register<Exec>("mavenInstall") {
    group = kmpBuildLogicPublishPluginGroup
    description = " - ?????????????"
    workingDir = rootProject.projectDir.resolve("libraries")
    commandLine = getMavenWindowsCommand() +
        listOf(
            "clean",
            "install",
            "-DskipTests",
        )
}

logger.info("Step 23: creation of task 'publishPluginToMavenLocal'")
tasks
    .register("publishPluginToMavenLocal") {
        group = kmpBuildLogicPublishPluginGroup
        description = " - Publishes Maven Publication 'KotlinMultiPlatformBuildLogicPublishPlugin' to the local Maven repository"
    }.also {
        rootProject.tasks.named("mavenInstall").configure {
            it
        }
    }

logger.info("Step 24: creation of task 'mavenPublish'")
tasks.register<Exec>("mavenPublish") {
    group = kmpBuildLogicPublishPluginGroup
    description = " - Publishes Maven Publication 'KotlinMultiPlatformBuildLogicPublishPlugin' to Maven Central Repository"
    workingDir = rootProject.projectDir.resolve("libraries")
    commandLine = getMavenWindowsCommand() +
        listOf(
            "clean",
            "deploy",
            "--activate-profiles=noTest",
            "-Dinvoker.skip=true",
            "-DskipTests",
            "-Ddeploy-snapshot-repo=local",
            "-Ddeploy-snapshot-url=file://${rootProject.projectDir.resolve("build/repo")}",
        )
}

logger.info("Step 18: Move assemble task to the build group")
tasks.named("assemble") {
    group = kmpBuildLogicBuildGroup
    description = "                  - Assembling the outputs of root."
}

logger.info("Step 19: Move build task to the build group")
tasks.named("build") {
    group = kmpBuildLogicBuildGroup
    description = "                     - Assembling and test root."
}

logger.info("Step 20: Move check task to the verification group")
tasks.named("check") {
    group = kmpBuildLogicCheckGroup
    description = "                     - Run all checks from root."
    dependsOn("test")
}

logger.info("Step 21: Move test task to the test group")
tasks.named("test") {
    group = kmpBuildLogicTestGroup
    description = "                      - Run the test suite of root."
}

logger.info("Step 22: Move buildDependents task to the build group")
tasks.named("buildDependents") {
    group = kmpBuildLogicBuildGroup
    description = "           - Assembles and tests this project and all projects that depend on it."
}

logger.info("Step 23: Move buildNeeded task to the build group")
tasks.named("buildNeeded") {
    group = kmpBuildLogicBuildGroup
    description = "               - Assembles and tests this project and all projects it depends on."
}

logger.info("Step 24: Move classes task to the build group")
tasks.named("classes") {
    group = kmpBuildLogicBuildGroup
    description = "                   - Assembles main classes."
}

logger.info("Step 25: Move jar task to the build group")
tasks.named("jar") {
    group = kmpBuildLogicBuildGroup
    description = "                       - Assembles a jar archive containing the classes of the 'main' feature."
}

logger.info("Step 26: Move testClasses task to the test group")
tasks.named("testClasses") {
    group = kmpBuildLogicTestGroup
    description = "               - Assembles test classes."
}

logger.info("Step 27: Move init task to the setup group")
tasks.named("init") {
    group = kmpBuildLogicSetupGroup
    description = "                      - Initializes a new Gradle build."
}

logger.info("Step 28: Move updateDaemonJvm task to the setup group")
tasks.named("updateDaemonJvm") {
    group = kmpBuildLogicSetupGroup
    description = "           - Generates or updates the Gradle Daemon JVM criteria."
}

logger.info("Step 29: Move wrapper task to the setup group")
tasks.named("wrapper") {
    group = kmpBuildLogicSetupGroup
    description = "                   - Generates Gradle wrapper files."
}

logger.info("Step 30: Move buildEnvironment task to the dependencies group")
tasks.named("buildEnvironment") {
    group = kmpBuildLogicDependenciesGroup
    description = "          - Displays all buildscript dependencies declared in this project."
}

logger.info("Step 31: Move dependencies task to the dependencies group")
tasks.named("dependencies") {
    group = kmpBuildLogicDependenciesGroup
    description = "              - Displays all dependencies declared in this project."
}

logger.info("Step 32: Move dependencyInsight task to the dependencies group")
tasks.named("dependencyInsight") {
    group = kmpBuildLogicDependenciesGroup
    description = "         - Displays the insight into a specific dependency in this project."
}

logger.info("Step 33: Move javaToolchains task to the check group")
tasks.named("javaToolchains") {
    group = kmpBuildLogicCheckGroup
    description = "            - Displays the detected java toolchains."
}

logger.info("Step 34: Move outgoingVariants task to the check group")
tasks.named("outgoingVariants") {
    group = kmpBuildLogicCheckGroup
    description = "          - Displays the outgoing variants of root project 'KMP-Build-Logic'."
}

logger.info("Step 35: Move resolvableConfigurations task to the check group")
tasks.named("resolvableConfigurations") {
    group = kmpBuildLogicCheckGroup
    description = "  - Displays the configurations that can be resolved in this project."
}

logger.info("Step 36: Change description of provisionDevelocityAccessKey task")
tasks.named("provisionDevelocityAccessKey") {
    description = "Provisions a new access key for this build environment."
    group = kmpBuildLogicDevelocityGroup
}

logger.info("Step 37: Change description of help task")
tasks.named("help") {
    description = "                      - Displays a help message."
    group = kmpBuildLogicHelpGroup
}

logger.info("Step 38: Change description of javadoc task")
tasks.named("javadoc") {
    description = "                   - Generates Javadoc API documentation for the 'main' feature."
    group = kmpBuildLogicDocumentationGroup
}

logger.info("Step 39: Deletes build directories with 'clean' task")
tasks.named<Delete>("clean") {
    description = "                     - Clean build directories."
    group = kmpBuildLogicBuildGroup
    delete(distDir)
    delete(layout.buildDirectory.dir("repository"))
}

/*
logger.info("Step 32: Move kotlinDslAccessorsReport task to no group")
tasks.named("kotlinDslAccessorsReport") {
    group = null
}

logger.info("Step 28: Remove buildScanPublishPrevious from tasks list")
tasks.named("buildScanPublishPrevious") {
    group = null
}

logger.info("Step 38: Change description of projects task")
tasks.named("projects") {
    description = "                    Displays the sub-projects of root project 'KMP-Build-Logic-Publish-Plugin'."
    group = kmpBuildLogicPublishHelpGroup
}

logger.info("Step 39: Change description of properties task")
tasks.named("properties") {
    description = "                  Displays the properties of root project 'KMP-Build-Logic-Publish-Plugin'"
    group = kmpBuildLogicPublishHelpGroup
}

logger.info("Step 41: Change description of tasks task")
tasks.named("tasks") {
    description = "                       Displays the tasks runnable from root project 'KMP-Build-Logic-Publish-Plugin'."
    group = kmpBuildLogicPublishCheckGroup
}

logger.info("Step 42: Change description of provisionGradleEnterpriseAccessKey task")
tasks.named("provisionGradleEnterpriseAccessKey") {
    group = kmpBuildLogicPublishDevelocityGroup
    }
    logger.info("Step 43: Remove provisionDevelocityAccessKey from tasks list")
    named("provisionDevelocityAccessKey") {
        group = null
    }
    logger.info("Step 44: Remove provisionGradleEnterpriseAccessKey")
    named("provisionGradleEnterpriseAccessKey") {
        group = null
    }

    fun aggregateTask(
        name: String,
        groupName: String,
        descriptionContent: String,
        projectTask: String,
        projects: List<String>,
    ) = register(name) {
        group = groupName
        description = descriptionContent
        projects.forEach { dependsOn("$it:$projectTask") }
    }

    val publishable = listOf(":plugin")
    val buildable = listOf(":plugin")
    val rootModule = listOf(":")

    aggregateTask(
        "generatePomPluginFile",
        kmpBuildLogicPublishPluginGroup,
        "       Generates the Maven POM file for the 'KMP-Build-Logic-Publish-Plugin'.",
        // "generatePomFileForPublishingPluginPluginMarkerMavenPublication",
        "generatePomFileForPluginMavenPublication",
        publishable,
    )
    aggregateTask(
        "generateMetadataPluginFile",
        kmpBuildLogicPublishPluginGroup,
        "  Generates the Gradle metadata file for the 'KMP-Build-Logic-Publish-Plugin'.",
        // "generateMetadataFileForPluginPluginMarkerMavenPublication",
        "generateMetadataFileForPluginMavenPublication",
        publishable,
    )
    aggregateTask(
        "buildScan",
        kmpBuildLogicPublishDevelocityGroup,
        "                   Publishes the data captured by the last build.",
        "buildScanPublishPrevious",
        rootModule,
    )
    aggregateTask(
        "cleanup",
        kmpBuildLogicPublishBuildGroup,
        "                     Deletes previous created builds.",
        "clean",
        publishable + buildable,
    )
    aggregateTask(
        "assemblePlugin",
        kmpBuildLogicPublishBuildGroup,
        "              Assembling the outputs of the plugin.",
        "assemble",
        buildable,
    )
    aggregateTask(
        "buildPlugin",
        kmpBuildLogicPublishBuildGroup,
        "                 Assembling and test the plugin.",
        "build",
        buildable,
    )
    aggregateTask(
        "buildDependentsPlugin",
        kmpBuildLogicPublishBuildGroup,
        "       Assembles and tests the plugin project and all projects that depend on it.",
        "buildDependents",
        buildable,
    )
    aggregateTask(
        "publishing",
        kmpBuildLogicPublishPluginGroup,
        "                  Publishing the plugin",
        "publish",
        publishable,
    )
    aggregateTask(
        "checking",
        kmpBuildLogicPublishPluginGroup,
        "                    Checking the plugin",
        "check",
        buildable,
    )
    aggregateTask(
        "buildNeededPlugin",
        kmpBuildLogicPublishBuildGroup,
        "           Assembles and tests the plugin and all projects it depends on.",
        "buildNeeded",
        buildable,
    )
    aggregateTask(
        "publishPluginToMavenCentral",
        kmpBuildLogicPublishPluginGroup,
        " Publishes Maven publication 'KMPBuildLogicPublishPlugin' to the central Maven repository.",
        "publishPublishingPluginPluginMarkerMavenPublicationToMavenCentralRepository",
        publishable,
    )


}

fun getMavenWindowsCommand(): List<String> =
    when {
        org.gradle.internal.os.OperatingSystem
            .current()
            .isWindows -> listOf("cmd", "/c", "mvnw.cmd")
        else -> listOf("./mvnw")
    }
*/

logger.info("\n")
logger.info("    ==========                                  EXECUTION PHASE OF LOGIC                                 ==========\n")
