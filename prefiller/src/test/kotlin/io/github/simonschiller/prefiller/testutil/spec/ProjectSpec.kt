/*
 * Copyright 2020 Simon Schiller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.simonschiller.prefiller.testutil.spec

import java.io.File

interface ProjectSpec {
    val versionCatalog: VersionCatalog

    fun getSettingsGradleContent(): String
    fun getGradlePropertiesContent(): String
    fun getLocalPropertiesContent(): String

    fun getRootBuildGradleContent(): String
    fun getModuleBuildGradleContent(): String

    fun getAndroidManifestContent(): String
    fun getScriptFileContent(): String

    fun getEntityClassName(): String
    fun getEntityClassContent(): String
    fun getDatabaseClassName(): String
    fun getDatabaseClassContent(): String

    fun generateAdditionalFiles(rootDir: File)
}
