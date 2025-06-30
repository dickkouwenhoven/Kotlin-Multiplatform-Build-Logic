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

package io.github.dickkouwenhoven.kmpbuildlogic.settings.tools

import io.github.dickkouwenhoven.kmpbuildlogic.settings.model.property.SyntheticPropertiesGenerator
import io.github.dickkouwenhoven.kmpbuildlogic.settings.model.property.PropertyDefinition
import io.github.dickkouwenhoven.kmpbuildlogic.settings.model.property.propertyDetectValueType
import io.github.dickkouwenhoven.kmpbuildlogic.settings.model.property.PropertyValue
import io.github.dickkouwenhoven.kmpbuildlogic.settings.model.property.PropertyValue.Configured
import io.github.dickkouwenhoven.kmpbuildlogic.settings.model.property.PropertyValue.Overridden
import io.github.dickkouwenhoven.kmpbuildlogic.settings.model.property.PropertyValue.UserDefined
import io.github.dickkouwenhoven.kmpbuildlogic.settings.model.property.PropertyValueType
import io.github.dickkouwenhoven.kmpbuildlogic.settings.model.SetupFile
import java.io.File
import java.io.StringReader
import java.util.Properties
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

const val LOCAL_PROPERTIES_FIRST_TEXT_LINE_OF_SYNCED_AREA = "# Start of checked local.properties area."
const val LOCAL_PROPERTIES_LAST_TEXT_LINE_OF_SYNCED_AREA = "# End of checked local.properties area."
private const val LOCAL_PROPERTIES_SECOND_TEXT_LINE_OF_SYNCED_AREA =
    "# A property value mentioned here overrules the equivalent variable one within gradle.properties."
private const val LOCAL_PROPERTIES_WARNING_MESSAGE_FIRST_LINE =
    "# Below properties are created by the system. Changes made here will be lost."
private const val LOCAL_PROPERTIES_WARNING_MESSAGE_SECOND_LINE = "# Place your properties above this area."

open class LocalPropertiesModifier(
    private val localProperties: File,
) {
    private val logger: Logger = Logging.getLogger(javaClass)
    private val initialUserConfiguredPropertiesContent = getUserConfiguredPropertiesContent()

    fun applySetup(
        setupFile: SetupFile,
        gradlePropertyResolver: GradlePropertyResolver,
        versionRequirementsChecker: VersionRequirementsChecker,
        syntheticPropertiesGenerators: List<SyntheticPropertiesGenerator> = emptyList(),
    ) {
        localProperties.parentFile.apply {
            if (!exists()) {
                mkdirs()
            }
        }
        check(localProperties.isFile) {
            "     ❌ $localProperties exists but is not a file!"
        }
        logger.lifecycle("     📍 The file 'local.properties' exists")
        val localPropertiesContent = getUserConfiguredPropertiesContent()
        val manuallyConfiguredProperties =
            Properties().apply {
                StringReader(localPropertiesContent).use {
                    load(it)
                }
            }

        val setupFileProperties: Map<String, PropertyDefinition> = setupFile.properties
        val syntheticProperties: Map<String, PropertyDefinition> = generateSyntheticProperties(syntheticPropertiesGenerators, setupFile)
        val combined = setupFileProperties + syntheticProperties

        val propertiesToSetup: Map<String, PropertyValue> = combined.mapValues { (key, definition) ->
            // Priority: CLI > systemProp > gradle.properties > env
            val gradlePropertiesValue: String? = gradlePropertyResolver.resolve(key)
            val localPropertiesValue = manuallyConfiguredProperties[key]
            val userDefinedValue = when {
                gradlePropertiesValue == null && localPropertiesValue == null -> null
                gradlePropertiesValue != null && localPropertiesValue == null -> gradlePropertiesValue
                gradlePropertiesValue == null && localPropertiesValue != null -> localPropertiesValue
                else -> localPropertiesValue
            }
            if (userDefinedValue != null) {
                when (definition.valueType) {
                    PropertyValueType.BOOLEAN -> {
                        if(userDefinedValue == "false" || userDefinedValue == "true") {
                            logUserDefinedValue(logger, userDefinedValue.toString())
                            UserDefined(userDefinedValue.toString())
                        } else {
                            logger.debug(
                                "Default will be used, because defined value is not of the type boolean. " +
                                "The value will be set on: ${definition.defaultValue}"
                            )
                            Overridden(userDefinedValue.toString(), definition.defaultValue)
                        }                        
                    }
                    PropertyValueType.INTEGER -> {
                        if (key == "org.gradle.jvm.version") {
                            if (propertyDetectValueType(userDefinedValue.toString()) == PropertyValueType.INTEGER) {
                                if (versionRequirementsChecker.isVersionValid(userDefinedValue.toString(), definition.versionRequirements.strictly)) {
                                    logUserDefinedValue(logger, userDefinedValue.toString())
                                    UserDefined(userDefinedValue.toString())
                                } else {
                                    logDefaultValue(logger, definition.defaultValue)
                                    Overridden(userDefinedValue.toString(), definition.defaultValue)
                                }
                            } else {
                                logIntegerDefaultValue(logger, definition.defaultValue)
                                Overridden(userDefinedValue.toString(), definition.defaultValue)
                            }
                        } else {
                            if (key == "kmpbuildlogic.properties.version.code") {
                                if (propertyDetectValueType(userDefinedValue.toString()) == PropertyValueType.INTEGER) {
                                    logUserDefinedValue(logger, userDefinedValue.toString())
                                    UserDefined(userDefinedValue.toString())
                                } else {
                                    logIntegerDefaultValue(logger, definition.defaultValue)
                                    Overridden(userDefinedValue.toString(), definition.defaultValue)
                                }
                            } else {
                                logUserDefinedValue(logger, userDefinedValue.toString())
                                UserDefined(userDefinedValue.toString())
                            }
                        }                        
                    }
                    PropertyValueType.STRING -> {
                        if (userDefinedValue.toString() == definition.defaultValue) {
                            logUserDefinedValue(logger, userDefinedValue.toString())
                            UserDefined(userDefinedValue.toString())
                        } else {
                            if (userDefinedValue.toString() in definition.allowedValues) {
                                logUserDefinedValue(logger, userDefinedValue.toString())
                                UserDefined(userDefinedValue.toString())
                            } else {
                                logDefaultValue(logger, definition.defaultValue)
                                Overridden(userDefinedValue.toString(), definition.defaultValue)
                            }
                        }                        
                    }
                    PropertyValueType.VERSION -> {
                        if (versionRequirementsChecker.isVersionValid(userDefinedValue.toString(), definition.versionRequirements.strictly)) {
                            logUserDefinedValue(logger, userDefinedValue.toString())
                            UserDefined(userDefinedValue.toString())
                        } else {
                            if (versionRequirementsChecker.isVersionValid(userDefinedValue.toString(), definition.versionRequirements.require)) {
                                logUserDefinedValue(logger, userDefinedValue.toString())
                                UserDefined(userDefinedValue.toString())
                            } else {
                                logDefaultValue(logger, definition.defaultValue)
                                Overridden(userDefinedValue.toString(), definition.defaultValue)
                            }
                        }                                                     
                    }
                }                
            } else {
                logger.lifecycle(
                    "Default will be used, because the property was not set. " +
                    "The value will be set on: ${definition.defaultValue}"
                )
                Configured(definition.defaultValue)
            }            
        }
        logger.lifecycle("     📍 Retrieved user configured properties")

        propertiesToSetup.forEach { (key, valueWrapper) ->
            when (valueWrapper) {
                is Overridden -> logger.lifecycle(
                    "Key '{}' with value '${valueWrapper.overridingValue}' will be overridden by '${valueWrapper.overridingValue}'",
                     key
                )
                is Configured -> logger.lifecycle(
                    "Key '{}' was not defined.\nThe value of it is set to its default value: '{}'",
                    key,
                    valueWrapper.value
                )
                is UserDefined -> logger.debug(
                    "Property: $key is defined by the user and in line with the requirements. No need for adding it."
                )
            }
        }

        localProperties.writeText(
            """
            |${localPropertiesContent.addSuffix()}
            |$LOCAL_PROPERTIES_FIRST_TEXT_LINE_OF_SYNCED_AREA
            |$LOCAL_PROPERTIES_SECOND_TEXT_LINE_OF_SYNCED_AREA
            |$LOCAL_PROPERTIES_WARNING_MESSAGE_FIRST_LINE
            |$LOCAL_PROPERTIES_WARNING_MESSAGE_SECOND_LINE
            |${propertiesToSetup.asPropertiesLines}
            |$LOCAL_PROPERTIES_LAST_TEXT_LINE_OF_SYNCED_AREA
            |
            """.trimMargin(),
        )

        val sizeOfConfigured = propertiesToSetup.values.count { it is Configured }
        val sizeOfOverridden = propertiesToSetup.values.count { it is Overridden }
        val amountOfRecords = sizeOfConfigured + sizeOfOverridden
        logger.lifecycle("     📍 Wrote local.properties with $amountOfRecords keys\n")
    }

    private fun getUserConfiguredPropertiesContent(): String {
        if (!localProperties.exists()) return ""
        var insideAutomaticallyConfiguredSection = false
        // filter out the automatically configured lines
        return localProperties
            .readLines()
            .filter { line ->
                if (line == LOCAL_PROPERTIES_FIRST_TEXT_LINE_OF_SYNCED_AREA || line == LOCAL_PROPERTIES_SECOND_TEXT_LINE_OF_SYNCED_AREA) {
                    insideAutomaticallyConfiguredSection = true
                }
                val shouldIncludeThisLine = !insideAutomaticallyConfiguredSection
                if (line == LOCAL_PROPERTIES_LAST_TEXT_LINE_OF_SYNCED_AREA) {
                    insideAutomaticallyConfiguredSection = false
                }
                shouldIncludeThisLine
            }.joinToString(System.lineSeparator())
    }
     
    private fun generateSyntheticProperties(
        syntheticPropertiesGenerators: List<SyntheticPropertiesGenerator>,
        setupFile: SetupFile,
    ): Map<String, PropertyDefinition> {
        val duplicatedKeys = mutableSetOf<String>()
        val result =
            syntheticPropertiesGenerators.fold(mapOf<String, PropertyDefinition>()) { acc, generator ->
                val newProperties = generator.generate(setupFile.properties)
                duplicatedKeys.addAll(acc.keys.intersect(newProperties.keys))
                acc + newProperties
            }
        require(duplicatedKeys.isEmpty()) {
            "These keys were defined previously: ${duplicatedKeys.joinToString()}"
        }
        return result
    }

    open fun initiallyContains(line: String) = initialUserConfiguredPropertiesContent.contains(line)

    open fun putLine(line: String) = localProperties.appendText("\n$line")
}

private fun String.addSuffix(): String {
    if (this.endsWith("\n")) return this
    return "$this$\n"
}

internal val Map<String, PropertyValue>.asPropertiesLines: String
    get() =
        filterValues { it !is UserDefined }
            .map { (key, valueWrapper) ->
                when (valueWrapper) {
                    is Overridden -> "$key = ${valueWrapper.overridingValue} #The original value '${valueWrapper.value}' is been overridden"
                    is Configured -> "$key = ${valueWrapper.value} # This property was not defined "
                    is UserDefined -> "$key = ${valueWrapper.value}"
            }
        }.joinToString(System.lineSeparator())

private fun logUserDefinedValue(
    logger: Logger,
    userDefinedValue: String
) {
    logger.debug("User defined value will be used. Value is: {}", userDefinedValue)
}

private fun logDefaultValue(
    logger: Logger,
    defaultValue: String
) {
    logger.debug(
        "Default will be used, because defined value does not meet the requirements. " +
        "The value will be set on: {}", defaultValue
    )
}

private fun logIntegerDefaultValue(
    logger: Logger,
    defaultValue: String
) {
    logger.debug(
        "Default will be used, because defined value is not of type integer. " +
        "The value will be set on: {}", defaultValue
    )
}