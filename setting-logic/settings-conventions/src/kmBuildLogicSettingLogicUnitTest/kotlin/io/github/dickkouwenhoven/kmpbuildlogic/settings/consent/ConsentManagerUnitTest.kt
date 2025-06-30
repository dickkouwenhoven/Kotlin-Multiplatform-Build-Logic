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

import io.github.dickkouwenhoven.kmpbuildlogic.settings.model.property.MockLocalPropertiesModifier
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConsentManagerUnitTest {
    lateinit var tempDir: File
    private val rootDir = tempDir

    @Test
    fun `getUserDecision returns true when consent marker exists`() {
        val modifier = MockLocalPropertiesModifier(
            existingLines = USER_CONSENT_MARKER,
            localProperties = rootDir.resolve("local.properties")
        )
        val manager = ConsentManager(modifier)
        assertTrue(manager.getUserDecision()!!)
    }

    @Test
    fun `getUserDecision returns false when refusal marker exists`() {
        val modifier = MockLocalPropertiesModifier(
            existingLines = USER_REFUSAL_MARKER,
            localProperties = rootDir.resolve("local.properties")
        )
        val manager = ConsentManager(modifier)
        assertFalse(manager.getUserDecision()!!)
    }

    @Test
    fun `getUserDecision returns true when globalConsentGiven is true`() {
        val manager = ConsentManager(
            MockLocalPropertiesModifier(
                existingLines = USER_REFUSAL_MARKER,
                localProperties = rootDir.resolve("local.properties")
            ), globalConsentGiven = true)
        assertTrue(manager.getUserDecision()!!)
    }

    @Test
    fun `getUserDecision returns null when no marker and no globalConsentGiven`() {
        val manager = ConsentManager(
            MockLocalPropertiesModifier(
                existingLines = USER_REFUSAL_MARKER,
                localProperties = rootDir.resolve("local.properties")
            )
        )
        assertNull(manager.getUserDecision())
    }

    @Test
    fun `applyConsentDecision true adds consent line`() {
        val modifier = MockLocalPropertiesModifier(
            existingLines = USER_REFUSAL_MARKER,
            localProperties = rootDir.resolve("local.properties")
        )
        val output = ByteArrayOutputStream()
        val manager = ConsentManager(modifier, output = PrintStream(output))
        manager.applyConsentDecision(true)
        assertTrue(modifier.putLines.first().contains(USER_CONSENT_MARKER))
    }

    @Test
    fun `applyConsentDecision false adds refusal line`() {
        val modifier = MockLocalPropertiesModifier(
            existingLines = USER_REFUSAL_MARKER,
            localProperties = rootDir.resolve("local.properties")
        )
        val output = ByteArrayOutputStream()
        val manager = ConsentManager(modifier, output = PrintStream(output))
        manager.applyConsentDecision(false)
        assertEquals(USER_REFUSAL_MARKER, modifier.putLines.first())
    }
}
