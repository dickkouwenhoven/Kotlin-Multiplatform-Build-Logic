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
 *
 */
package io.github.dickkouwenhoven.kmpbuildlogic.plugins

import io.github.dickkouwenhoven.kmpbuildlogic.annotations.GradlePortalPublishingPluginDsl
import io.github.dickkouwenhoven.kmpbuildlogic.publish.gradle.portal.plugin.KmpBuildLogicPublishGradlePortalPluginPublishingExtension
import io.github.dickkouwenhoven.kmpbuildlogic.publish.gradle.portal.plugin.KmpBuildLogicPublishGradlePortalPluginPublishingExtension.GradlePortalPublication
import io.github.dickkouwenhoven.kmpbuildlogic.publish.gradle.portal.plugin.KmpBuildLogicPublishGradlePortalPluginPublishingSetup
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import java.net.Inet6Address
import java.net.InetAddress
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.UnknownHostException
import kotlin.KotlinVersion
import kotlin.jvm.javaClass

internal data class ParsedGradleVersion(val major: Int, val minor: Int) : Comparable<ParsedGradleVersion> {
    override fun compareTo(other: ParsedGradleVersion): Int {
        val majorCompare = major.compareTo(other.major)
        if (majorCompare != 0) return majorCompare

        return minor.compareTo(other.minor)
    }

    companion object {
        private fun String.parseIntOrNull(): Int? =
            try {
                toInt()
            } catch (e: NumberFormatException) {
                Logging.getLogger(javaClass).debug("⚠️ Number Format Exception", e)
                null
            }

        fun parse(version: String): ParsedGradleVersion? {
            val matches = "(\\d+)\\.(\\d+).*"
                .toRegex()
                .find(version)
                ?.groups
                ?.drop(1)?.take(2)
                // checking if two subexpression groups are found and length of each is >0 and <4
                ?.let { it -> if (it.all { it -> (it?.value?.length ?: 0).let { it > 0 && it < 4 } }) it else null }

            val versions = matches?.mapNotNull { it?.value?.parseIntOrNull() } ?: emptyList()
            if (versions.size == 2 && versions.all { it >= 0 }) {
                val (major, minor) = versions
                return ParsedGradleVersion(major, minor)
            }

            return null
        }
    }
}

private fun isGradleVersionAtLeast(major: Int, minor: Int) =
    ParsedGradleVersion.parse(GradleVersion.current().version)
        ?.let { it >= ParsedGradleVersion(major, minor) }
        ?: false
@GradlePortalPublishingPluginDsl
public class GradlePortalPublishingPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension =
            project.extensions.create(
                "gradlePortalPublishing",
                KmpBuildLogicPublishGradlePortalPluginPublishingExtension::class.java,
                project.objects,
            )

        project.afterEvaluate {
            // Step 1. Prerequisite checks
            checkGradlePortalLoginCredentials()
            checkGradleVersion()
            checkJvmVersion()
            checkKotlinVersion()
            checkRequiredPlugins()

            val publications = extension.gradlePortalPublications

            // Step 2: Check for duplicate artifactsIds
            val duplicates =
                publications
                    .groupBy { it.artifactId }
                    .filter { it.value.size > 1 }

            require(duplicates.isEmpty()) {
                "Duplicate artifactIds found: ${duplicates.keys.joinToString()}."
            }

            // Step 3: Loop over all publications defined by the user
            publications.forEach { publication ->

                // Step 4: Validate all user-defined fields in the publication

                // pluginName checks
                validatePluginName(
                    pluginName = publication.pluginName,
                )

                // pluginName checks
                validatePluginName(
                    pluginName = publication.pluginName,
                )

                // groupId checks
                validateGroupId(
                    groupId = publication.groupId,
                    pluginName = publication.pluginName,
                )

                // artifactId checks
                validateArtifactId(
                    artifactId = publication.artifactId,
                    pluginName = publication.pluginName,
                )

                // implementationClass checks
                validateImplementationClass(
                    implementationClass = publication.implementationClass,
                    publication = publication,
                )

                // pluginVersion checks
                validateVersion(
                    version = publication.pluginVersion,
                    publication = publication,
                )

                // displayName checks
                validateDisplayName(
                    displayName = publication.pluginInformation.displayName,
                    publication = publication,
                )

                // description checks
                validateDescription(
                    description = publication.pluginInformation.description,
                    publication = publication,
                )

                // pluginTags checks
                checkNotNull(publication.pluginInformation.pluginTags) {
                    "PluginTags are required for publication '${publication.pluginName}' and must not be null."
                }
                validatePluginTags(
                    pluginTags = publication.pluginInformation.pluginTags,
                    pluginName = publication.pluginName,
                )

                // website checks
                validateHttpsUrl(
                    value = publication.scm.website,
                    fieldName = "Website",
                    pluginName = publication.pluginName,
                    publication = publication,
                    project = project,
                )

                // URL checks (Version Control System)
                validateHttpsUrl(
                    value = publication.scm.url,
                    fieldName = "URL",
                    pluginName = publication.pluginName,
                    publication = publication,
                    project = project,
                )

                // Step 5: Setup publishing configuration for this publication
                KmpBuildLogicPublishGradlePortalPluginPublishingSetup.configure(
                    project = project,
                    publication = publication
                )
            }
        }
    }
}

private fun Project.resolveGroupId(userDefinedGroupId: String?): String =
    when {
        !userDefinedGroupId.isNullOrBlank() -> userDefinedGroupId
        project.group.toString().isNotBlank() -> project.group.toString()
        project.findProperty("group")?.toString()?.isNotBlank() == true -> project.findProperty("group").toString()
        project.findProperty("packageName")?.toString()?.isNotBlank() == true -> project.findProperty("packageName").toString()
        else -> throw IllegalArgumentException("groupId could not be resolved. Please set it explicitly.")
    }

private fun checkGradleVersion() {
    val minimumGradleVersion = System.getProperty("org.gradle.minimum.version") ?: "8.13"
    val parts = minimumGradleVersion.split(".")
    val majorGradleVersion = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minorGradleVersion = parts.getOrNull(1)?.toIntOrNull() ?: 0
    require(isGradleVersionAtLeast(majorGradleVersion, minorGradleVersion)) {
        "Gradle version must be at least $minimumGradleVersion, but found ${GradleVersion.current().version}."
    }
}

private fun checkJvmVersion() {
    val minimumJvmVersion = System.getProperty("org.gradle.minimum.jvm.version")
    val currentJvmVersion = JavaVersion.current()
    val isValid = currentJvmVersion >= JavaVersion.toVersion(minimumJvmVersion)
    require(isValid) {
        "Jvm version must be at least $minimumJvmVersion, but found ${currentJvmVersion.name}."
    }
}

private fun checkKotlinVersion() {
    val minimumKotlinVersion = System.getProperty("kotlin.minimum.language.version")
    val parts = minimumKotlinVersion.split(".")
    val majorKotlinVersion = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minorKotlinVersion = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val patchKotlinVersion = parts.getOrNull(2)?.toIntOrNull() ?: 0
    val currentKotlinVersion = KotlinVersion.CURRENT
    val isValid = currentKotlinVersion >= KotlinVersion(majorKotlinVersion, minorKotlinVersion, patchKotlinVersion)
    require(isValid) {
        "Kotlin version must be at least $minimumKotlinVersion, but found $currentKotlinVersion."
    }
}

private fun Project.checkRequiredPlugins() {
    val minimumKotlinVersion = System.getProperty("kotlin.minimum.language.version")
    val parts = minimumKotlinVersion.split(".")
    val majorKotlinVersion = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minorKotlinVersion = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val patchKotlinVersion = parts.getOrNull(2)?.toIntOrNull() ?: 0
    val requiredPlugins = listOf("com.gradle.plugin-publish", "org.jetbrains.jvm")
    requiredPlugins.forEach { pluginId ->
        require(project.plugins.hasPlugin(pluginId)) {
            "Required plugin $pluginId is missing."
        }
        // Check if the used plugin is making use of a supported Kotlin version
        if (!isAtLeastKotlinVersion(pluginId, majorKotlinVersion, minorKotlinVersion, patchKotlinVersion)) {
            error("You need Kotlin version $minimumKotlinVersion or newer.")
        }
    }
}

private fun Project.isAtLeastKotlinVersion(
    pluginId: String,
    major: Int,
    minor: Int,
    patch: Int,
): Boolean {
    val plugin = project.plugins.getPlugin(pluginId) as KotlinBasePlugin
    val elements = plugin.pluginVersion.takeWhile { it != '-' }.split(".")
    val pluginMajor = elements[0].toInt()
    val pluginMinor = elements[1].toInt()
    val pluginPatch = elements[2].toInt()
    return KotlinVersion(
        pluginMajor,
        pluginMinor,
        pluginPatch,
    ) >=
        KotlinVersion(
            major,
            minor,
            patch,
        )
}

private fun validateHttpsUrl(
    value: String,
    fieldName: String,
    pluginName: String,
    publication: GradlePortalPublication,
    project: Project,
) {
    require(value.isNotBlank()) {
        "$fieldName is required for publication '$pluginName' and must not be null or blank."
    }
    try {
        val uri = URI(value)
        require(uri.toURL().protocol == "https") {
            "$fieldName must use the https protocol."
        }
        val host = uri.host
        // Check for IPv6 syntax if present
        if (host != null && host.startsWith("[") && host.endsWith("]")) {
            val ipv6 = host.removePrefix("[").removeSuffix("]")
            require(isValidIPv6Address(ipv6)) {
                "$fieldName contains an invalid IPv6 address: $ipv6"
            }
        }
    } catch (e: MalformedURLException) {
        throw IllegalArgumentException("$fieldName is not valid: ${e.message}")
    } catch (e: URISyntaxException) {
        throw IllegalArgumentException("$fieldName has invalid URI syntax: ${e.message}")
    }
    require(value.length <= 2000) {
        "$fieldName exceeds the maximum length of 2000 characters."
    }

    if (fieldName == "Website") {
        project.logger.info(
            "No Website Url is been specified. Falling back to default value: 'https://github.com/group/name'\n" +
                "If not correct, please set the website.",
        )
        val group = project.group.toString()
        val name = project.name
        val groupName = "$group/$name"
        publication.scm.website = "https://github.com/$groupName"
    }
    if (fieldName == "URL") {
        project.logger.info(
            "No Version Control System Url is been specified. Falling back to default value: 'WebsiteUrl.git'\n" +
                "If not correct, please set the url.",
        )
        publication.scm.url = "${publication.scm.website}.git"
    }
}

// Basic IPv6 format validator
private fun isValidIPv6Address(ip: String): Boolean =
    try {
        val address = InetAddress.getByName(ip)
        address is Inet6Address
    } catch (e: Exception) {
        false
    }

private fun validatePluginTags(
    pluginTags: List<String?>,
    pluginName: String,
) {
    require(pluginTags.isNotEmpty()) {
        "PluginTags are required for publication '$pluginName'and must contain at least one tag"
    }
    pluginTags.forEachIndexed { index, tag ->
        require(tag!!.isNotBlank()) {
            "PluginTag at index [$index] are required for publication '$pluginName' and must not be blank."
        }
        require(tag.all { it.isLetterOrDigit() || it == '-' || it == '_' }) {
            "PluginTag at index [$index] must only contain letters, digits, hyphens, or underscores."
        }
        require(tag.length in 3..50) {
            "PluginTag at [$index] must be between 3 and 50 characters long."
        }
    }
}

private fun Project.validateDescription(
    description: String?,
    publication: GradlePortalPublication,
) {
    val pluginName = publication.pluginName
    val pluginDescription =
        when {
            description!!.isNotBlank() -> description
            !project.description.isNullOrBlank() -> project.description.toString()
            else -> {
                project.logger.info(
                    "No plugin description is been specified. Falling back to default value: 'Gradle Portal plugin 'project.name''\n" +
                        "If not correct, please set description.",
                )
                "Gradle Portal Plugin ${project.name}"
            }
        }
    publication.pluginInformation.description = pluginDescription

    require(pluginDescription.isNotBlank()) {
        "A description is required for publication '$pluginName' and may not be null or blank."
    }
    require(pluginDescription.length >= 20) {
        "The description is required for publication '$pluginName' and must contain at least 20 characters."
    }
    require(pluginDescription.length <= 500) {
        "The description is required for publication '$pluginName' and must not exceed 500 characters."
    }
    require(pluginDescription.contains("\n")) {
        "The description must not contain newlines."
    }
    require(pluginDescription.contains("\t")) {
        "The description must not contain tabs."
    }
    require(pluginDescription.all { it.code in 32..126 }) {
        "The description must only contain printable ASCII characters."
    }
}

private fun Project.validateDisplayName(
    displayName: String?,
    publication: GradlePortalPublication,
) {
    val pluginName = publication.pluginName
    val pluginDisplayName =
        when {
            displayName!!.isNotBlank() -> displayName
            else -> {
                project.logger.info(
                    "No displayName is been specified. Falling back to default value: 'Gradle Portal Plugin for ${project.name}'\n" +
                        "If not correct, please set displayName.",
                )
                "Gradle Portal Plugin for ${project.name}"
            }
        }
    require(pluginDisplayName.isNotBlank()) {
        "A displayName is required for publication '$pluginName' and may not be null or blank."
    }
    require(pluginDisplayName.length >= 3) {
        "The displayName is required for publication '$pluginName' and must contain at least 3 characters."
    }
    require(pluginDisplayName.length <= 50) {
        "The displayName is required for publication '$pluginName' and must not exceed 50 characters."
    }
    require(pluginDisplayName.contains("\n")) {
        "The displayName must not contain newlines."
    }
    require(pluginDisplayName.contains("\t")) {
        "The displayName must not contain tabs."
    }
    require(pluginDisplayName.trim().split("\\s+".toRegex()).size <= 8) {
        "The displayName must not contain more then 8 words."
    }
    require(pluginDisplayName.all { it.code in 32..126 }) {
        "The displayName must only contain printable ASCII characters."
    }
    publication.pluginInformation.displayName = pluginDisplayName
}

private fun Project.validateVersion(
    version: String?,
    publication: GradlePortalPublication,
) {
    val pluginName = publication.pluginName
    val pluginVersion =
        when {
            version!!.isNotBlank() -> version
            project.version != Project.DEFAULT_VERSION -> project.version.toString()
            rootProject.hasProperty("version") -> rootProject.property("version").toString()
            else -> {
                project.logger.info(
                    "No plugin version is been specified. Falling back to default value: '1.0.0'\n" +
                        "If not correct, please set pluginVersion in your build.gradle.kts or define 'version' in rootProject 'gradle.properties'.",
                )
                "1.0.0"
            }
        }
    require(pluginVersion.isNotBlank()) {
        "pluginVersion must be set either explicitly or via project.version for '$pluginName'."
    }
    val semverRegex = Regex("""^\d+\.\d+(-[a-zA-Z0-9.]+)?$""")
    require(pluginVersion.matches(semverRegex)) {
        "pluginVersion '$pluginVersion' does not follow recommended semantic versioning."
    }
    require(!pluginVersion.endsWith("-SNAPSHOT")) {
        "Snapshot versions are not allowed for plugin publication: $pluginVersion."
    }
    publication.pluginVersion = pluginVersion
}

private fun Project.validateImplementationClass(
    implementationClass: String?,
    publication: GradlePortalPublication,
) {
    val pluginName = publication.pluginName
    val basePackage = project.group.toString()
    val capitalizedName = project.name.replaceFirstChar { it.uppercaseChar() }
    val tempImplementationClass = "$basePackage.${capitalizedName}Plugin"
    val pluginImplementationClass =
        when {
            implementationClass!!.isNotBlank() -> implementationClass
            project.group.toString().isNotBlank() || project.name.isNotBlank() -> {
                project.logger.info(
                    "No 'implementationClass' is been specified. Falling back to default value: 'group.namePlugin'\n" +
                        "If not correct, please set implementationClass'.",
                )
                tempImplementationClass
            }
            else -> {
                throw IllegalArgumentException(
                    "No 'implementationClass' or 'project.group' or 'project.name' are been specified.'\n" +
                        "You need to set 'implementationClass' or within your build.gradle.kts set 'project.group' or 'project.name'.",
                )
            }
        }
    require(pluginImplementationClass.isNotBlank()) {
        "An implementationClass is required for publication '$pluginName' and may not be null or blank."
    }
    val implementationClassPattern = Regex("""^([a-zA-Z_][\w$]*\.)+[A-Z][\w$]*$""")
    require(pluginImplementationClass.matches(implementationClassPattern)) {
        "Invalid implementationClass format: '$pluginImplementationClass."
    }
    publication.implementationClass = pluginImplementationClass
}

private fun validateArtifactId(
    artifactId: String?,
    pluginName: String,
) {
    require(!artifactId.isNullOrBlank()) {
        "An artifactId is required for publication '$pluginName' and may not be null or blank."
    }
    require(artifactId.length >= 3) {
        "The artifactId is required for publication '$pluginName' and must contain at least 3 characters."
    }
    require(artifactId.length <= 50) {
        "The artifactId is required for publication '$pluginName' and must not exceed 50 characters."
    }
    val artifactIdPattern = Regex("^[a-z0-9\\-]+$")
    require(artifactId.matches(artifactIdPattern)) {
        "The artifactId is required for publication '$pluginName' and must contain" +
            "only lowercase letters, digits, and hyphens."
    }
}

private fun Project.validateGroupId(
    groupId: String?,
    pluginName: String,
) {
    val resolveGroupId =
        resolveGroupId(
            groupId,
        )
    require(resolveGroupId.isNotBlank()) {
        "A description is required for publication '$pluginName' and may not be null or blank."
    }
    require(resolveGroupId.length <= 100) {
        "The groupId is required for publication '$pluginName' and must not exceed 100 characters."
    }
    require(!resolveGroupId.contains("\n")) {
        "The groupId is required for publication '$pluginName' and must not contain newlines."
    }
    require(!resolveGroupId.contains("\t")) {
        "The groupId is required for publication '$pluginName' and must not contain tabs."
    }
    require(resolveGroupId.matches(Regex("^[a-z0-9]+(\\.[a-z0-9]+)*$"))) {
        "The groupId must follow reverse domain format, using only lowercase letters, numbers, and dots (e.g. 'com.example')."
    }
    require(resolveGroupId.all { it.code in 32..126 }) {
        "The groupId is required for publication '$pluginName' and must only contain printable ASCII characters."
    }
    require('.' in resolveGroupId) {
        "The groupId is required for publication '$pluginName' and should follow reverse DNS" +
            "style and containing at least one dot."
    }
    val domainPart = resolveGroupId.substringBefore(".")
    val resolved =
        try {
            InetAddress.getByName(domainPart)
            true
        } catch (_: UnknownHostException) {
            false
        }
    if (!resolved) {
        project.logger.warn(
            "The domain ´$domainPart' in groupId '$groupId' could not be resolved.\n" +
                "Make sure you're using a domain you control to avoid plugin ID conflicts.",
        )
    }
}

private fun validatePluginName(pluginName: String?) {
    require(!pluginName.isNullOrBlank()) {
        "A pluginName is required for publication and may not be null or blank."
    }
    require(pluginName.length <= 64) {
        "The pluginName should not exceed 64 characters."
    }
    require(!pluginName.contains("\n")) {
        "The pluginName must not contain newlines."
    }
    require(!pluginName.contains("\t")) {
        "The pluginName must not contain tabs."
    }
    require(pluginName.trim().split("\\s+".toRegex()).size <= 5) {
        "The pluginName must not contain more then 5 words."
    }
    require(pluginName.all { it.code in 32..126 }) {
        "The pluginName must only contain printable ASCII characters."
    }
}

private fun checkGradlePortalLoginCredentials() {
    val gradlePortalPublishKey = System.getenv("GRADLE_PUBLISH_KEY")
    val gradlePortalPublishSecret = System.getenv("GRADLE_PUBLISH_SECRET")
    require(!gradlePortalPublishKey.isNullOrBlank()) {
        "An API key for the Gradle Portal is required for publication\n" +
            "'GRADLE_PUBLISH_KEY' as environment variable is needed and may not be null or blank"
    }
    require(!gradlePortalPublishSecret.isNullOrBlank()) {
        "An API key for the Gradle Portal is required for publication\n" +
            "'GRADLE_PUBLISH_SECRET' as environment variable is needed and may not be null or blank"
    }
}
