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

/**
 * [SyntheticPropertiesGenerator] generates additional properties based on a 'setup' file and local environment.
 * Should be stateless.
 *
 * @since 1.0
 */
interface SyntheticPropertiesGenerator {
    /**
     * [generate] function extends [SyntheticPropertiesGenerator]
     *
     * It reads the initial value of consent
     *
     * @property setupFile A 'file' that contains all the properties.
     *
     * @since 1.0
     */
    fun generate(setupFile: Map<String, PropertyDefinition>): Map<String, PropertyDefinition>
}
