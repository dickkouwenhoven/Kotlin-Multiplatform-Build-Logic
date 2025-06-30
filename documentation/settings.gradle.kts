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

logger.info("\n")
logger.info("    ==========                     Start of settings.gradle.kts from documentation                     ==========\n")

logger.info("Step 1 : plugin settings.")
plugins {
    alias(libs.plugins.kmpBuildLogicSettingsPlugin)
}

logger.info("Step 1 : 'rootProject name' setting.")
val mainRootProjectName: String? = System.getProperty("kmpbuildlogic.properties.root.project.name")
val kmpbuildlogicDocumentationProjectName = mainRootProjectName +
    "-" +
    "Documentation"
rootProject.name = kmpbuildlogicDocumentationProjectName

logger.info("\n")
logger.info("    ==========                      End of settings.gradle.kts from documentation                      ==========\n")
