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

import io.github.dickkouwenhoven.kmpbuildlogic.settings.KMPBUILDLOGIC_PROPERTIES_CONSENT_DECISION_GRADLE_PROPERTY
import org.gradle.api.GradleException
/**
 * [CannotRequestConsentWithinIdeException] takes care of providing a consent exception message.
 *
 * The functionality of getting a consent from a user can not be achieved within an IDE.
 * User needs to be within the Command Line Interface (CLI).
 *
 * @property consentDetailsLink A 'link' reference towards a text block that contains consent details.
 *
 * @since 1.0.0
 * @author Dick Kouwenhoven
 */
class CannotRequestConsentWithinIdeException(
    consentDetailsLink: String?,
) : GradleException(
        """
    |${USER_CONSENT_REQUEST}
    |The consent cannot be requested in interactive mode when running from IDE.
    |Please either invoke Gradle from the command line or add -P${KMPBUILDLOGIC_PROPERTIES_CONSENT_DECISION_GRADLE_PROPERTY}=(true,false)
    |to the run parameters in order to make a decision.
    |${if (consentDetailsLink != null) USER_CONSENT_DETAILS_LINK_TEMPLATE.formatWithLink(consentDetailsLink) else ""}
        """.trimMargin(),
    )