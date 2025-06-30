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

package io.github.dickkouwenhoven.kmpbuildlogic.settings.model

import io.github.dickkouwenhoven.kmpbuildlogic.settings.model.property.PropertyDefinition
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

/**
 * [SetupFile] is a data class that contains all data that is used to create the 'local.properties' file.
 *
 * @property properties Is a variable that contains all the properties with its details.
 * @property consentDetailsLink Is a string that contains a link to a text block with content about consent.
 * @property obfuscationSalt Is ????????
 *
 * @since 1.0.0
 * @author Dick Kouwenhoven
 */
@Serializable
data class SetupFile(
    val properties: Map<String, PropertyDefinition>,
    val consentDetailsLink: String? = null,
    val obfuscationSalt: String? = null,
)

/**
 * [json] is a variable that contains the settings needed for reading the requirements json file [parseSetupFile].
 *
 * @since 1.0.0
 * @author Dick Kouwenhoven
 */
@OptIn(ExperimentalSerializationApi::class)
private val json =
    Json {
        ignoreUnknownKeys = true
        namingStrategy = JsonNamingStrategy.SnakeCase
    }

/**
 * [parseSetupFile] is the function that reads the requirements json file in the format of the [SetupFile].
 *
 *
 * @since 1.0.0
 * @author Dick Kouwenhoven
 */
@OptIn(ExperimentalSerializationApi::class)
internal fun parseSetupFile(inputStream: InputStream): SetupFile {
    return Json.decodeFromStream<SetupFile>(inputStream)
}

