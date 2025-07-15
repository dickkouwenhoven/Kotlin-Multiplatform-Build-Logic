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

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

public class KmpBuildLogicPublishGradlePortalPluginPublishingSetup {
    public companion object {
        public fun configure(
            project: Project,
            publication: KmpBuildLogicPublishGradlePortalPluginPublishingExtension.GradlePortalPublication,
        ) {
            project.logger.info("Start GradlePortalPublishingSetup")

            // Set version
            project.version = publication.pluginVersion

            // Set group
            project.group = publication.groupId
            project.logger.info("Set group to ${publication.groupId}")

            // Plugin publishing metadata
            configureGradlePluginMetadata(
                project,
                publication,
            )

            project.logger.info("Finished GradlePortalPublishingSetup")
        }

        private fun configureGradlePluginMetadata(
            project: Project,
            publication: KmpBuildLogicPublishGradlePortalPluginPublishingExtension.GradlePortalPublication,
        ) {
            project.extensions.configure<GradlePluginDevelopmentExtension> {
                website.set(publication.scm.website)
                vcsUrl.set(publication.scm.url)

                plugins {
                    create(publication.pluginName) {
                        id = "${publication.groupId}.${publication.artifactId}"
                        implementationClass = publication.implementationClass
                        displayName = publication.pluginInformation.displayName
                        description = publication.pluginInformation.description
                        tags.set(publication.pluginInformation.pluginTags)
                    }
                }
            }
        }
    }
}
