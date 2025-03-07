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

package io.github.simonschiller.prefiller

import com.google.common.truth.Truth.assertThat
import io.github.simonschiller.prefiller.internal.util.Version
import io.github.simonschiller.prefiller.testutil.LanguageTestVersions
import io.github.simonschiller.prefiller.testutil.NoSchemaLocationTestVersions
import io.github.simonschiller.prefiller.testutil.ProjectExtension
import io.github.simonschiller.prefiller.testutil.TestVariants
import io.github.simonschiller.prefiller.testutil.compatibility.AgpVersionCompatibility.AGP_7_1_0
import io.github.simonschiller.prefiller.testutil.compatibility.AgpVersionCompatibility.AGP_7_3_0
import io.github.simonschiller.prefiller.testutil.outcomeOf
import io.github.simonschiller.prefiller.testutil.spec.DynamicFeatureProjectSpec
import io.github.simonschiller.prefiller.testutil.spec.NonAndroidProjectSpec
import io.github.simonschiller.prefiller.testutil.spec.ProjectSpec
import io.github.simonschiller.prefiller.testutil.spec.VersionCatalog
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class PrefillerIntegrationTest {

    @JvmField
    @RegisterExtension
    val project = ProjectExtension()

    @ParameterizedTest
    @ArgumentsSource(TestVariants::class)
    fun `Build fails if plugin is applied to non-Android projects`(versionCatalog: VersionCatalog) {
        project.setup(NonAndroidProjectSpec(versionCatalog))
        val result = project.run("clean", expectFailure = true)
        assertThat(result.output).contains("Prefiller is only applicable to Android projects")
    }

    @ParameterizedTest
    @ArgumentsSource(LanguageTestVersions::class)
    fun `Build fails if schema directory is not found`(projectSpec: ProjectSpec) {
        project.setup(projectSpec)

        var content = project.moduleBuildGradle.readText()
        content = content.replace("com.test.PeopleDatabase", "com.test.InvalidDatabase")
        project.moduleBuildGradle.writeText(content)

        val result = project.run("prefillPeopleDebugDatabase", expectFailure = true)
        assertThat(result.output).contains("'schemaDirectory'")
    }

    @ParameterizedTest
    @ArgumentsSource(LanguageTestVersions::class)
    fun `Build fails if SQL file is invalid`(projectSpec: ProjectSpec) {
        project.setup(projectSpec)
        project.scriptFile.appendText("Normal text is not a valid SQL statement")

        val result = project.run("prefillPeopleDebugDatabase", expectFailure = true)
        assertThat(result.output).contains("Could not parse script")
    }

    @ParameterizedTest
    @ArgumentsSource(NoSchemaLocationTestVersions::class)
    fun `Build fails if schema location is not configured`(projectSpec: ProjectSpec) {
        project.setup(projectSpec)
        val result = project.run("prefillPeopleDebugDatabase", expectFailure = true)
        assertThat(result.output).contains("Could not find schema location")
    }

    @ParameterizedTest
    @ArgumentsSource(LanguageTestVersions::class)
    fun `Pre-filled database file is generated`(projectSpec: ProjectSpec) {
        project.setup(projectSpec)
        project.run("prefillPeopleDebugDatabase")

        val databaseFile = project.moduleDir.resolve("build/generated/assets/prefillPeopleDebugDatabase/people.db")
        assertThat(databaseFile.exists()).isTrue()
    }

    @ParameterizedTest
    @ArgumentsSource(LanguageTestVersions::class)
    fun `Database is generated during normal build process`(projectSpec: ProjectSpec) {
        project.setup(projectSpec)
        project.run("assembleDebug")

        val databaseFile = project.moduleDir.resolve("build/generated/assets/prefillPeopleDebugDatabase/people.db")
        assertThat(databaseFile.exists()).isTrue()
    }

    @ParameterizedTest
    @ArgumentsSource(LanguageTestVersions::class)
    fun `Generated database is registered correctly as Android asset`(projectSpec: ProjectSpec) {
        project.setup(projectSpec)
        project.run("mergeDebugAssets")

        val agpBaseVersion = Version.parse(projectSpec.versionCatalog.agpVersion).baseVersion()
        val mergedAssetsPath = when {
            agpBaseVersion >= AGP_7_3_0 -> "assets/debug"
            agpBaseVersion >= AGP_7_1_0 -> "assets/debug/mergeDebugAssets"
            else -> "merged_assets/debug/out"
        }
        val mergedAssetsDir = project.moduleDir.resolve("build/intermediates/$mergedAssetsPath")
        assertThat(mergedAssetsDir.resolve("people.db").exists()).isTrue()
    }

    @ParameterizedTest
    @ArgumentsSource(LanguageTestVersions::class)
    fun `Up-to-date checks work correctly`(projectSpec: ProjectSpec) {
        project.setup(projectSpec)

        var result = project.run("prefillPeopleDebugDatabase")
        assertThat(result.tasks.outcomeOf("prefillPeopleDebugDatabase")).isEqualTo(TaskOutcome.SUCCESS)

        result = project.run("prefillPeopleDebugDatabase")
        assertThat(result.tasks.outcomeOf("prefillPeopleDebugDatabase")).isEqualTo(TaskOutcome.UP_TO_DATE)

        // Trigger change
        project.scriptFile.appendText("""
            INSERT INTO people(name, age) VALUES ("Jorja Maddox", 39);
        """.trimIndent())

        result = project.run("prefillPeopleDebugDatabase")
        assertThat(result.tasks.outcomeOf("prefillPeopleDebugDatabase")).isEqualTo(TaskOutcome.SUCCESS)

        result = project.run("prefillPeopleDebugDatabase")
        assertThat(result.tasks.outcomeOf("prefillPeopleDebugDatabase")).isEqualTo(TaskOutcome.UP_TO_DATE)
    }

    @ParameterizedTest
    @ArgumentsSource(TestVariants::class)
    fun `Dynamic feature modules work correctly`(versionCatalog: VersionCatalog) {
        project.setup(DynamicFeatureProjectSpec(versionCatalog))
        project.run(":module:prefillPeopleDebugDatabase")

        val databaseFile = project.moduleDir.resolve("build/generated/assets/prefillPeopleDebugDatabase/people.db")
        assertThat(databaseFile.exists()).isTrue()
    }
}
