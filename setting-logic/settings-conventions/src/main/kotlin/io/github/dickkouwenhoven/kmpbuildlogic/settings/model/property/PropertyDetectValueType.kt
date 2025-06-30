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
package io.github.dickkouwenhoven.kmpbuildlogic.settings.model.property

fun propertyDetectValueType(value: String): PropertyValueType = when {
    // General comment: IgnoreCase is not needed if value only contains numbers
    // Check if value is of type Boolean (examples: true, false)
    value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true) -> PropertyValueType.BOOLEAN
    // Checks if value is of type Version
    // Check if version is written as an 'alpha' version (example: 2.18.0-alpha01)
    value.matches(Regex(""".*[.-]alpha\d$""", RegexOption.IGNORE_CASE)) -> PropertyValueType.VERSION
    // Check if version is written as a 'beta' version (example: 2.18.0-beta01)
    value.matches(Regex(""".*[.-]beta\d$""", RegexOption.IGNORE_CASE)) -> PropertyValueType.VERSION
    // Check if version is written as integer (example: 21). 
    value.matches(Regex("""\d+""")) -> PropertyValueType.INTEGER
    // Check if version is written as numeric only version (examples: 4.0, 2.18.0)
    value.matches(Regex("""\d+\.\d+(\.\d+)?""")) -> PropertyValueType.VERSION
    // Check if version is written as rc version (example: 2.18.0-rc01)
    value.matches(Regex(""".*[.-]rc\d$""", RegexOption.IGNORE_CASE)) -> PropertyValueType.VERSION
    // Check if version is written as snapshot version (example: 2.18.0-snapshot)
    value.matches(Regex(""".*[.-]snapshot$""", RegexOption.IGNORE_CASE)) -> PropertyValueType.VERSION
    // Default propertyType value is of type String and/or is not one of above
    else -> PropertyValueType.STRING
}

