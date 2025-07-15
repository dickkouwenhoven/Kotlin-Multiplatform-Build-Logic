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
 *
 */
package io.github.dickkouwenhoven.kmpbuildlogic.publish.gradle.portal.plugin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

public open class KmpBuildLogicPublishGradlePortalPluginPublishingExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        public val gradlePortalPublications: NamedDomainObjectContainer<GradlePortalPublication> =
            objects.domainObjectContainer(GradlePortalPublication::class.java)

        public fun configureGradlePortalPublications(action: NamedDomainObjectContainer<GradlePortalPublication>.() -> Unit) {
            gradlePortalPublications.action()
        }

        public open class GradlePortalPublication
            @Inject
            constructor(
                public val pluginName: String,
                objects: ObjectFactory,
            ) {
                private val _groupId = objects.property<String>()
                public var groupId: String
                    get() = _groupId.get()
                    set(value) {
                        _groupId.set(value)
                    }

                private val _artifactId = objects.property<String>()
                public var artifactId: String
                    get() = _artifactId.get()
                    set(value) {
                        _artifactId.set(value)
                    }

                private val _implementationClass = objects.property<String>()
                public var implementationClass: String
                    get() = _implementationClass.get()
                    set(value) {
                        _implementationClass.set(value)
                    }

                private val _pluginVersion = objects.property<String>()
                public var pluginVersion: String
                    get() = _pluginVersion.get()
                    set(value) {
                        _pluginVersion.set(value)
                    }

                public val pluginInformation: GradlePortalPluginInformation =
                    objects.newInstance(GradlePortalPluginInformation::class.java)

                private fun configurePluginInformation(action: GradlePortalPluginInformation.() -> Unit) {
                    action(pluginInformation)
                }

                public val scm: Scm =
                    objects.newInstance(Scm::class.java)

                private fun configureScm(action: Scm.() -> Unit) {
                    scm.action()
                }
            }

        public open class GradlePortalPluginInformation
            @Inject
            constructor(
                objects: ObjectFactory,
            ) {
                private val _displayName = objects.property<String>()
                public var displayName: String
                    get() = _displayName.get()
                    set(value) {
                        _displayName.set(value)
                    }

                private val _description = objects.property<String>()
                public var description: String
                    get() = _description.get()
                    set(value) {
                        _description.set(value)
                    }

                private val _pluginTags = objects.property<List<String>>()
                public var pluginTags: List<String>
                    get() = _pluginTags.get()
                    set(value) {
                        _pluginTags.set(value)
                    }
            }

        public open class Scm
            @Inject
            constructor(
                objects: ObjectFactory,
            ) {
                private val _website = objects.property<String>()
                public var website: String
                    get() = _website.get()
                    set(value) {
                        _website.set(value)
                    }

                private val _url = objects.property<String>()

            public var url: String
                    get() = _url.get()
                    set(value) {
                        _url.set(value)
                    }
            }
    }
