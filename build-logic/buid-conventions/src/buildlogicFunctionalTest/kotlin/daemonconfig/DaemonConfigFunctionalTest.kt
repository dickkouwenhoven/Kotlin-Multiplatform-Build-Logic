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
package daemonconfig

import org.junit.jupiter.api.Test
import testkit.GradleTestKitRunner
import java.io.File

class DaemonConfigFunctionalTest {
    private val testProjectDir = File("src/functionalTest/resources/test-projects/daemon-config-test")

    @Test
    fun `plugin sets expected JVM arguments`() {
        val result = GradleTestKitRunner(testProjectDir).run("build")

        assert(result.output.contains("jvmEncoding is:"))
        assert(result.output.contains("jvmMemory is:"))
    }
}
