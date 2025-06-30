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
 * [PropertyValue] is a sealedClass that defines if a property was already [Configured] or that it is [Overridden].
 *
 * This class will be input to a 'propertiesReport'. With this class the 'propertiesReport' is able to present what
 * happens with the value of a property.
 * @property value Is one of the properties which will be labeled as [Configured] or [Overridden].
 *
 * @since 1.0.0
 * @author Dick Kouwenhoven
 */
internal sealed class PropertyValue(
    val value: String,
) {
    /**
     * [Configured] is a one of the three class labels which can be given to a property.
     *
     * Configured means that the property wasn´t set by the user and that it will be included.
     * 
     * @property value Is one of the properties which will be labeled as and [Configured] item.
     * @since 1.0.0
     * @author Dick Kouwenhoven
     */
    class Configured(
        value: String,
    ) : PropertyValue(value)

    /**
     * [Overridden] is a one of the three class labels which can be given to a property.
     *
     * Overridden means that the value defined by the user isn´t in line with the requirements and 
     * the user value will be given a default value.
     *   
     * @property value Is one of the properties which will be labeled as and [Overridden] item.
     * @property overridingValue Is a variable which will be filled with the original value.
     * It will be used in an overview report
     * @since 1.0.0
     * @author Dick Kouwenhoven
     */
    class Overridden(
        value: String,
        val overridingValue: String,
    ) : PropertyValue(value)

    /**
     * [UserDefined] is a one of the three class labels which can be given to a property.
     *
     * UserDefined means that the value defined by the user is in line with the requirements.
     *
     * @property value Is one of the properties which will be labeled as and [Overridden] item.
     * It will be used in an overview report.
     *
     * @since 1.0.0
     * @author Dick Kouwenhoven
     */
    class UserDefined(
        value: String,        
    ) : PropertyValue(value)
}
