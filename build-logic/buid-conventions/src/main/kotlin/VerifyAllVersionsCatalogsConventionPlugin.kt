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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.tooling.GradleConnector
import java.io.File

class VerifyAllVersionsCatalogsConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("verifyAllVersionCatalogs") {
            group = "KMP Build Logic Verification"
            description = "  - Generates verification metadata for all version catalogs using Gradle's Tooling API."
            val rootDir = project.rootDir
            val alphaVersionCatalogPath = File(rootDir, "gradle/alpha.versions.toml").absolutePath
            val betaVersionCatalogPath = File(rootDir, "gradle/beta.versions.toml").absolutePath
            val rcVersionCatalogPath = File(rootDir, "gradle/rc.versions.toml").absolutePath
            val gaVersionCatalogPath = File(rootDir, "gradle/libs.versions.toml").absolutePath

            doLast {
                val catalogProfiles =
                    listOf(
                        Triple("alpha", alphaVersionCatalogPath, "alpha.versions.toml"),
                        Triple("beta", betaVersionCatalogPath, "beta.versions.toml"),
                        Triple("rc", rcVersionCatalogPath, "rc.versions.toml"),
                        Triple("ga", gaVersionCatalogPath, "libs.versions.toml"),
                    )
                val total = catalogProfiles.size
                var current = 1
                println("🛠 Verifying version catalogs... ($total profiles)")
                catalogProfiles.forEach { (profile, versionCatalogPath, fileName) ->
                    println("[$current/$total] ⏳ Generating metadata for profile: $profile ($fileName)")
                    val start = System.currentTimeMillis()

                    val connector =
                        GradleConnector
                            .newConnector()
                            .forProjectDirectory(rootDir)

                    try {
                        connector.connect().use { connection ->
                            val build =
                                connection
                                    .newBuild()
                                    .forTasks("dependencies")
                                    .withArguments(
                                        "--write-verification-metadata",
                                        "sha512,pgp",
                                        "-PversionCatalogPath=$versionCatalogPath",
                                        "-Pkmpbuildlogic.properties.version.profile=$profile",
                                        "--info",
                                    )
                            build.run()
                        }
                        val duration = System.currentTimeMillis() - start
                        println("[$current/$total] ✅ Done for profile: $profile in ${duration}ms")
                    } catch (e: Exception) {
                        println("[$current/$total] ❌ Failed for profile: $profile: ${e.message}")
                    }
                    current++
                }
                println("🎉 All version catalog profiles processed.")
            }
        }
    }
}
