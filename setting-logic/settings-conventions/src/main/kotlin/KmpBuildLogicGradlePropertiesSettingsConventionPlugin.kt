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
package io.github.dickkouwenhoven.kmpbuildlogic.settings

import io.github.dickkouwenhoven.kmpbuildlogic.settings.consent.CannotRequestConsentWithinIdeException
import io.github.dickkouwenhoven.kmpbuildlogic.settings.consent.ConsentManager
import io.github.dickkouwenhoven.kmpbuildlogic.settings.model.parseSetupFile
import io.github.dickkouwenhoven.kmpbuildlogic.settings.tools.GradlePropertyResolver
import io.github.dickkouwenhoven.kmpbuildlogic.settings.tools.LocalPropertiesModifier
import io.github.dickkouwenhoven.kmpbuildlogic.settings.tools.VersionRequirementsChecker
import java.io.FileInputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

internal const val KMPBUILDLOGIC_PROPERTIES_CONSENT_DECISION_GRADLE_PROPERTY = "kmpbuildlogic.properties.consent.decision.gradle.property"

private val isInIdea
    get() = System.getProperty("idea.active").toBoolean()

abstract class KmpBuildLogicGradlePropertiesSettingsConventionPlugin : Plugin<Settings> {
    private val logger: Logger = Logging.getLogger(javaClass)

    override fun apply(settings: Settings) {
        val gradlePropertyResolver = GradlePropertyResolver(settings)
        val versionRequirementsChecker = VersionRequirementsChecker()
        try {
            logger.lifecycle("\nCompatibility check of Gradle Properties.\n")
            // The creation of a variable that is able to modify the local.properties file.
            // (the file where we will write all needed changes into).
            val modifier = LocalPropertiesModifier(
                localProperties = settings.rootDir.resolve("local.properties") 
            )
            // The KMP Build Logic requirements are written in the 'gradle.properties.json' file.
            val jsonFile = settings.rootDir.resolve("gradle/gradle.properties.json")
            if (!jsonFile.exists()) {
                logger.lifecycle("          ❌ The Gradle Properties JSON File 'gradle.properties.json' could not be found at ${jsonFile.path}, skipping the properties check")
                return
            }
            logger.lifecycle("     📍 Gradle Properties JSON File exists")
            // Use the 'jsonFile' which contains all properties and versions for KMP Build Logic to create a 'setupFile'.
            val setupFile = FileInputStream(jsonFile).buffered().use {
               parseSetupFile(it)
            }
            if (setupFile.properties.isEmpty()) {
                logger.lifecycle("     ❌ Setup File could not be filled with the data from the Gradle Properties JSON File")
            } else {
                logger.lifecycle("     📍 Setup File filled with data from Gradle Properties JSON File")
            }
            
            // We need consent from the user to make changes in properties
            // For this we create a consentManager to handle this process.
            // The user is also able to set up consent within the gradle.properties file
            // The first step is to see if that is being set or not.
            // Reference to the consent setting
            val kmpBuildLogicPropertiesSetupConsent =
                settings
                    .providers.gradleProperty("kmpbuildlogic.properties.consent.decision.gradle.property").orNull?.toBoolean()
            // Creation of the consentManager with reference as input
            val consentManager =
                ConsentManager(
                    modifier,
                    kmpBuildLogicPropertiesSetupConsent,
                )
            
            // Retrieve initial consent value
            val initialDecision = consentManager.getUserDecision()
            // When the user has already set consent as not agreed (false),
            // then further processing isn´t an option/ not needed anymore
            if (initialDecision == false) {
                logger.lifecycle("     ⚠️ Skipping automatic local.properties configuration as you've opted out")
                return
            }
            // When the user did not set consent, then start the process for asking consense to the user.
            if (initialDecision == null) {
                val nonInteractiveDecision =
                    settings.providers
                        .gradleProperty(
                            KMPBUILDLOGIC_PROPERTIES_CONSENT_DECISION_GRADLE_PROPERTY,
                        ).map { it.toBoolean() }
                if (isInIdea && !nonInteractiveDecision.isPresent) {
                    throw CannotRequestConsentWithinIdeException(setupFile.consentDetailsLink)
                }
                val consentReceived =
                    if (nonInteractiveDecision.isPresent) {
                        consentManager.applyConsentDecision(nonInteractiveDecision.get(), setupFile.consentDetailsLink)
                    } else {
                        consentManager.askForConsent(setupFile.consentDetailsLink)
                    }
                if (!consentReceived!!) {
                    logger.lifecycle("     ⚠️ Skipping automatic local.properties configuration as the consent wasn't given")
                    return
                }
            }
            logger.lifecycle("     📍 Consent for automatic local.properties configuration is given")
            // Now the consent is given by the user,
            // we can start working on the details through the modifier 'LocalPropertiesModifier'.
            modifier.applySetup(
                setupFile,
                gradlePropertyResolver,
                versionRequirementsChecker
            )
            logger.lifecycle("✅ Automatic local.properties configuration setup has been applied.\n")
        } catch (e: CannotRequestConsentWithinIdeException) {
            throw e
        } catch (e: UnknownHostException) {
            logger.debug("     ⚠️ Cannot connect to the internal properties storage", e)
        } catch (e: SocketTimeoutException) {
            logger.debug("     ⚠️ Cannot connect to the internal properties storage", e)
        } catch (e: Throwable) {
            logger.warn("     ⚠️ Something went wrong during the automatic local.properties setup attempt", e)
        }
    }
}

