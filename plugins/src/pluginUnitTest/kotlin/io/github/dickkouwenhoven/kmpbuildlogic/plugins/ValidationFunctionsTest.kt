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
package io.github.dickkouwenhoven.kmpbuildlogic.plugins

import io.github.dickkouwenhoven.kmpbuildlogic.publish.gradle.portal.plugin.GradlePortalPublishingExtension
import org.gradle.api.Project
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

abstract class ValidationFunctionsTest : Project {
    @Test
    fun doAValidateHttpsUrlTest() {
        val extension = extensions.getByName("gradlePortalPublishing") as GradlePortalPublishingExtension
        val publications = extension.gradlePortalPublications

        publications.forEach { publication ->

            // Valid URL
            validateHttpsUrl("https://example.com", "Website", "TestPlugin", publication, project)

            // Invalid URL (not HTTPS)
            assertThrows(IllegalArgumentException::class.java) {
                validateHttpsUrl("http://example.com", "Website", "TestPlugin", publication, project)
            }

            // Invalid URL (malformed)
            assertThrows(IllegalArgumentException::class.java) {
                validateHttpsUrl("htp://example.com", "Website", "TestPlugin", publication, project)
            }

            // URL exceeding length
            val longUrl = "https://example.com/" + "a".repeat(2000)
            assertThrows(IllegalArgumentException::class.java) {
                validateHttpsUrl(longUrl, "Website", "TestPlugin", publication, project)
            }
        }
    }

    @Test
    fun doAValidatePluginTagsTest() {
        // Valid tags
        validatePluginTags(listOf("tag1", "tag-2", "tag_3"), "TestPlugin")

        // Empty tag list
        assertThrows(IllegalArgumentException::class.java) {
            validatePluginTags(emptyList(), "TestPlugin")
        }

        // Blank tag
        assertThrows(IllegalArgumentException::class.java) {
            validatePluginTags(listOf(" "), "TestPlugin")
        }

        // Tag with invalid characters
        assertThrows(IllegalArgumentException::class.java) {
            validatePluginTags(listOf("tag!"), "TestPlugin")
        }

        // Tag too short
        assertThrows(IllegalArgumentException::class.java) {
            validatePluginTags(listOf("ta"), "TestPlugin")
        }

        // Tag too long
        val longTag = "a".repeat(51)
        assertThrows(IllegalArgumentException::class.java) {
            validatePluginTags(listOf(longTag), "TestPlugin")
        }
    }

    @Test
    fun doAValidateDescriptionTest() {
        val extension = extensions.getByName("gradlePortalPublishing") as GradlePortalPublishingExtension
        val publications = extension.gradlePortalPublications

        publications.forEach { publication ->

            // Valid description
            validateDescription("This is a valid description.", publication)

            // Null description
            assertThrows(IllegalArgumentException::class.java) {
                validateDescription(null, publication)
            }

            // Blank description
            assertThrows(IllegalArgumentException::class.java) {
                validateDescription(" ", publication)
            }

            // Description too short
            assertThrows(IllegalArgumentException::class.java) {
                validateDescription("Short desc", publication)
            }

            // Description too long
            val longDescription = "a".repeat(501)
            assertThrows(IllegalArgumentException::class.java) {
                validateDescription(longDescription, publication)
            }

            // Description with newline
            assertThrows(IllegalArgumentException::class.java) {
                validateDescription("Invalid\nDescription", publication)
            }

            // Description with tab
            assertThrows(IllegalArgumentException::class.java) {
                validateDescription("Invalid\tDescription", publication)
            }

            // Description with non-ASCII characters
            assertThrows(IllegalArgumentException::class.java) {
                validateDescription("Invalid\u0001Description", publication)
            }
        }
    }
}
