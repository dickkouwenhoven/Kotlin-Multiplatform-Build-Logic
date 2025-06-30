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

import java.util.regex.Pattern

class VersionRequirementsChecker() {

    fun isVersionValid(actual: String?, range: String?): Boolean {
        if (actual == null || actual == "") {
            return false
        }
        if (range == null || range == "") {
            return false
        }
        // Example: [20,24), [1.8.10,2.1.21[
        val pattern = Pattern.compile("""([\[(])([\d.]+),([\d.]+)([)\]])""")
        val matcher = pattern.matcher(range)

        if (!matcher.matches()) return true // Fail open for now

        val lowerInclusive = matcher.group(1) == "["
        val lowerBound = matcher.group(2)
        val upperBound = matcher.group(3)
        val upperInclusive = matcher.group(4) == "]"

        val cmpLower = compareVersions(actual, lowerBound)
        val cmpUpper = compareVersions(actual, upperBound)

        val lowerOk = if (lowerInclusive) cmpLower >= 0 else cmpLower > 0
        val upperOk = if (upperInclusive) cmpUpper <= 0 else cmpUpper < 0

        return lowerOk && upperOk
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val p1 = v1.split(".")
        val p2 = v2.split(".")

        for (i in 0 until maxOf(p1.size, p2.size)) {
            val a = p1.getOrNull(i)?.toIntOrNull() ?: 0
            val b = p2.getOrNull(i)?.toIntOrNull() ?: 0
            if (a != b) return a.compareTo(b)
        }
        return 0
    }
}
