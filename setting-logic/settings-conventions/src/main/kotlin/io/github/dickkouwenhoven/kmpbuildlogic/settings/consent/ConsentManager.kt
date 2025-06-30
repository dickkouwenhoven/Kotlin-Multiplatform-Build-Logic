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
package io.github.dickkouwenhoven.kmpbuildlogic.settings.consent

import io.github.dickkouwenhoven.kmpbuildlogic.settings.tools.LocalPropertiesModifier
import java.io.BufferedReader
import java.io.PrintStream
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

private val isInIdeaSync
    get() = System.getProperty("idea.sync.active").toBoolean()

private const val linkPlaceholder = "%LINK%"

const val USER_CONSENT_MARKER = "# This line indicates that you have chosen to enable automatic configuration of properties."

internal const val USER_CONSENT_MARKER_WITH_DETAILS_LINK = "$USER_CONSENT_MARKER Details: $linkPlaceholder"

const val USER_REFUSAL_MARKER =
    "# This line indicates that you have chosen to disable automatic configuration of properties. If you want to enable it, remove this line."

internal val USER_CONSENT_REQUEST =
    """

    ⚠️ ATTENTION REQUIRED ⚠️
    We are asking for your consent for automatic configuration of local.properties file
    for providing some optimizations to ensure KMP Build Logic can operate properly.
    """.trimIndent()

internal const val USER_CONSENT_DETAILS_LINK_TEMPLATE = "You can read more details here: $linkPlaceholder"

internal const val PROMPT_REQUEST = "Do you agree with this? Please answer with 'yes' or 'no': "

private const val MAX_REQUEST_ATTEMPTS = 5

internal fun String.formatWithLink(link: String) = replace(linkPlaceholder, link)

/**
 * [ConsentManager] handles consent logic.
 *
 * It checks system properties, gradle.properties and local.properties.
 *
 * Note:
 * - If 'Command Line Interface' (CLI) then default 'value' is: 'FALSE'.
 * - If not CLI AND there is no consent value available then a request for consent will be shown.
 *
 * When a consent is given it updates 'local.properties' file accordingly.
 * @property modifier The modifier which is responsible for the interactions with:
 * 'files': Examples are: 'gradle.properties', 'local.properties' and
 * 'variables': Examples are: 'org.gradle.jvm.version' and 'kotlin.language.version'.
 * @property globalConsentGiven A variable set or provided by CLI by the user or given by the user.
 * @property input A keyboard reader to retrieve the user response on the question for consent.
 * @property output A screenwriter to present consent request and consent summary.
 *
 * @since 1.0
 * @author Dick Kouwenhoven
 *
 */
class ConsentManager(
    val modifier: LocalPropertiesModifier,
    val globalConsentGiven: Boolean? = null,
    val input: BufferedReader = System.`in`.bufferedReader(),
    val output: PrintStream = System.out,
) {
    private val logger: Logger = Logging.getLogger(javaClass)

    /**
     * [getUserDecision] function extends [ConsentManager]
     *
     * It reads the initial value of consent
     *
     * @since 1.0
     * @author Dick Kouwenhoven
     */
    fun getUserDecision(): Boolean? {
        return when {
            modifier.initiallyContains(USER_REFUSAL_MARKER) -> {
                logger.debug("User refusal marker detected.")
                false
            }
            modifier.initiallyContains(USER_CONSENT_MARKER) -> {
                logger.debug("User consent marker detected.")
                true
            }
            globalConsentGiven == false -> {
                logger.debug("User refusal through globalConsentGiven marker detected")
                false
            }
            globalConsentGiven == true -> {
                logger.debug("User consent through globalConsentGiven marker detected")
                true
            }
            isInIdeaSync -> {
                logger.debug("IDE sync detected, skipping consent request.")
                false
            }
            else -> {
                logger.debug("User consent or refusal could not be detected")
                null
            }
        }
    }

    /**
     * [printConsentRequest] function extends [ConsentManager]
     *
     * Function displays a text block which contains a request for consent
     *
     * @property consentDetailsLink A 'link' reference towards a text block that contains consent details.
     *
     * @since 1.0
     * @author Dick Kouwenhoven
     */
    private fun printConsentRequest(consentDetailsLink: String? = null) {
        output.println()
        output.println(USER_CONSENT_REQUEST)
        if (consentDetailsLink != null) {
            output.println(USER_CONSENT_DETAILS_LINK_TEMPLATE.formatWithLink(consentDetailsLink))
        }
        output.println()
    }

    /**
     * [applyConsentDecision] function extends [ConsentManager]
     *
     * This function displays a text block which shows the outcome of the user consent value given.
     *
     * @property consentGiven A variable of type 'boolean' which contains the consent value.
     * @property consentDetailsLink A 'link' reference towards a text block that contains consent details.
     *
     * @since 1.0
     * @author Dick Kouwenhoven
     */
    fun applyConsentDecision(
        consentGiven: Boolean,
        consentDetailsLink: String? = null,
    ): Boolean {
        if (consentGiven) {
            val line = consentDetailsLink?.let {
                USER_CONSENT_MARKER_WITH_DETAILS_LINK.formatWithLink(it)
            } ?: USER_CONSENT_MARKER
            logger.debug("Consent given for automatic configuration.")
            modifier.putLine(line)
        } else {
            logger.lifecycle("Consent refused by the user.")
            output.println("You've refused to give the consent for the automatic configuration of local.properties")
            modifier.putLine(USER_REFUSAL_MARKER)
        }
        return consentGiven
    }

    /**
     * [askForConsent] function extends [ConsentManager]
     *
     * An interaction function which take care of consent handling with the user.
     *
     * @property consentDetailsLink A 'link' reference towards a text block that contains consent details.
     *
     * @since 1.0.0
     * @author Dick Kouwenhoven
     */
    fun askForConsent(consentDetailsLink: String? = null): Boolean? {
        printConsentRequest(consentDetailsLink)
        repeat(MAX_REQUEST_ATTEMPTS) {
            output.println(PROMPT_REQUEST)
            val response = input.readLine()?.trim()?.lowercase()
            return when (response) {
                "yes" -> applyConsentDecision(true, consentDetailsLink)
                "no" -> applyConsentDecision(false, consentDetailsLink)
                else -> return@repeat
            }
        }
        logger.warn("No valid input after $MAX_REQUEST_ATTEMPTS attempts.")
        return null // no consent decision made
    }
}
