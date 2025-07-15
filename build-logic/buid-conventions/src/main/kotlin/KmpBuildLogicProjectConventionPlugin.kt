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

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.embeddedKotlinVersion
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KmpBuildLogicProjectConventionPlugin : Plugin<Project> {
    private val logger = Logging.getLogger(javaClass)
    val javaVersion =
        JavaVersion.toVersion(
            System.getProperty("org.gradle.jvm.version"),
        )

    override fun apply(project: Project) {
        with(project) {
            with(pluginManager) {
                apply(JavaLibraryPlugin::class.java)
                apply("dokkatoo.convention.plugin")
                apply("jvm.toolchain.evaluate.convention.plugin")
                // apply("io.github.dickkouwenhoven.kmpbuildlogic.publish.plugin")
                apply("org.jetbrains.kotlin.jvm")
            }
            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }

            extensions.configure<KotlinProjectExtension> {
                explicitApi = ExplicitApiMode.Strict
            }

            // extensions.configure(AbiValidationExtension::class.java) {
            //    @OptIn(ExperimentalAbiValidation::class)
            //    enabled.set(true)
            // }

            with(tasks) {
                withType(KotlinCompile::class.java) {
                    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
                }
                named("check") {
                    dependsOn("checkLegacyAbi")
                }
            }
        }
    }
}
