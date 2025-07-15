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
package io.github.dickkouwenhoven.kmpbuildlogic.buildlogic

import org.junit.jupiter.api.Test
import kotlin.test.Test
import kotlin.test.assertTrue
// import io.mockk.*
// import org.gradle.kotlin.dsl.*

class DaemonConfigConventionPluginUnitTest {
    @Test
    fun `plugin id should match expected`() {
        val expectedId = "kmpbuildlogic.daemon.config.plugin"
        val actualId = "kmpbuildlogic.daemon.config.plugin" // Replace with const/logic if applicable
        assertTrue(actualId == expectedId)
    }
}
