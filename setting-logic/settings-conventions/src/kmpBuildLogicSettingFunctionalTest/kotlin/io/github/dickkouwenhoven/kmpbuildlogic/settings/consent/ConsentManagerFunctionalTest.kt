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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
// import java.io.*

class ConsentManagerFunctionalTest {

    @Test
    fun `functional test simulating CLI user refusal`() {
        val input = BufferedReader(StringReader("maybe\nno\n"))
        val output = ByteArrayOutputStream()
        val modifier = MockLocalPropertiesModifier()
        val manager = ConsentManager(modifier, input = input, output = PrintStream(output))

        val result = manager.askForConsent("https://my.link")
        val outputText = output.toString()

        assertFalse(result!!)
        assertTrue(outputText.contains("refused"))
        assertEquals(USER_REFUSAL_MARKER, modifier.putLines.first())
    }
}

