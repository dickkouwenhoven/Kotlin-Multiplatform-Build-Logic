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
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.io.StringReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class ConsentManagerIntegrationTest {
    lateinit var tempDir: File
    private val rootDir = tempDir

    @Test
    fun `askForConsent accepts yes and stores consent`() {
        val input = BufferedReader(StringReader("yes\n"))
        val output = ByteArrayOutputStream()
        val modifier = MockLocalPropertiesModifier(
            existingLines = USER_REFUSAL_MARKER,
            localProperties = rootDir.resolve("local.properties")
        )
        val manager = ConsentManager(modifier, input = input, output = PrintStream(output))

        val result = manager.askForConsent("https://example.com")
        assertTrue(result!!)
        assertTrue(modifier.putLines.first().contains("https://example.com"))
    }

    @Test
    fun `askForConsent accepts no and stores refusal`() {
        val input = BufferedReader(StringReader("no\n"))
        val output = ByteArrayOutputStream()
        val modifier = MockLocalPropertiesModifier(
            existingLines = USER_REFUSAL_MARKER,
            localProperties = rootDir.resolve("local.properties")
        )
        val manager = ConsentManager(modifier, input = input, output = PrintStream(output))

        val result = manager.askForConsent()
        assertFalse(result!!)
        assertEquals(USER_REFUSAL_MARKER, modifier.putLines.first())
    }

    @Test
    fun `askForConsent returns null after 5 invalid attempts`() {
        val input = BufferedReader(StringReader("maybe\n123\n??\nnope\nyesno\n"))
        val output = ByteArrayOutputStream()
        val modifier = MockLocalPropertiesModifier(
            existingLines = USER_REFUSAL_MARKER,
            localProperties = rootDir.resolve("local.properties")
        )
        val manager = ConsentManager(modifier, input = input, output = PrintStream(output))

        val result = manager.askForConsent()
        assertNull(result)
        assertTrue(modifier.putLines.isEmpty())
    }
}
