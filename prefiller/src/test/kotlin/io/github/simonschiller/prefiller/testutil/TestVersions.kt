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

package io.github.simonschiller.prefiller.testutil

import io.github.simonschiller.prefiller.internal.util.Version
import io.github.simonschiller.prefiller.testutil.AgpVersionCompatibility.agpIsCompatibleWithRuntime
import io.github.simonschiller.prefiller.testutil.GradleVersionCompatibility.gradleIsCompatibleWithRuntime
import io.github.simonschiller.prefiller.testutil.spec.JavaProjectSpec
import io.github.simonschiller.prefiller.testutil.spec.KotlinKaptProjectSpec
import io.github.simonschiller.prefiller.testutil.spec.KotlinKspProjectSpec
import io.github.simonschiller.prefiller.testutil.spec.NoSchemaLocationJavaProjectSpec
import io.github.simonschiller.prefiller.testutil.spec.NoSchemaLocationKotlinKaptProjectSpec
import io.github.simonschiller.prefiller.testutil.spec.NoSchemaLocationKotlinKspProjectSpec
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.stream.Stream

open class TestVersions : ArgumentsProvider {
    private val logger: Logger = LoggerFactory.getLogger(TestVersions::class.java)

    // See https://gradle.org/releases
    private val gradleVersions = listOf(
        "8.4",
        "8.4",
        "8.3",
        "8.2.1",
        "8.1.1",
        "8.0.2",
        "7.6.3",
        "7.5.1",
        "7.4.2",
        "7.3.3",
        "7.2",
        "7.1.1",
        "7.0.2",
        "6.9.2",
        "6.8.3",
        "6.7.1",
        "6.6.1",
        "6.5.1",
        "6.4.1",
        "6.3",
        "6.2.2",
        "6.1.1",
    )

    // See https://developer.android.com/studio/releases/gradle-plugin
    private val agpVersions = listOf(
        "8.1.4",
        "8.0.2",
        "7.4.2",
        "7.3.1",
        "7.2.2",
        "7.1.3",
        "7.0.4",
        "4.2.2",
    )

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
        val arguments = getCompatibleGradleAgpVersions()
            .map { (gradleVersion, agpVersion) -> Arguments.of(gradleVersion.toString(), agpVersion.toString()) }
            .toList()

        require(arguments.isNotEmpty()) {
            "Found no compatible AGP and Gradle version combination, check your supplied arguments."
        }

        return arguments.stream()
    }

    private fun getCompatibleGradleAgpVersions(): Sequence<Pair<Version, Version>> {
        val (gradleCompatibleVersions, gradleIncompatibleVersions) = gradleVersions().partition {
            gradleIsCompatibleWithRuntime(it.baseVersion())
        }

        if (gradleIncompatibleVersions.isNotEmpty()) {
            logger.warn(
                "Gradle versions {} cannot be run on the current JVM `{}`",
                gradleIncompatibleVersions.joinToString(),
                Runtime.version()
            )
        }

        val (agpCompatibleVersions, agpIncompatibleVersions) = agpVersions().partition {
            agpIsCompatibleWithRuntime(it)
        }

        if (agpIncompatibleVersions.isNotEmpty()) {
            logger.warn(
                "Android Gradle Plugin versions {} cannot be run on the current JVM `{}`",
                agpIncompatibleVersions.joinToString(),
                Runtime.version()
            )
        }

        return sequence {
            gradleCompatibleVersions.forEach { gradleVersion ->
                agpCompatibleVersions.forEach { agpVersion ->
                    yield(gradleVersion to agpVersion)
                }
            }
        }.filter { (gradleVersion, agpVersion) ->
            AgpVersionCompatibility.agpIsCompatibleWithGradle(agpVersion, gradleVersion)
        }
    }

    // Allow setting a single, fixed Gradle version via environment variables
    private fun gradleVersions(): List<Version> {
        val gradleVersion = System.getenv("GRADLE_VERSION")
        return if (gradleVersion == null) {
            gradleVersions.map(Version::parse)
        } else {
            listOf(Version.parse(gradleVersion))
        }
    }

    // Allow setting a single, fixed AGP version via environment variables
    private fun agpVersions(): List<Version> {
        val agpVersion = System.getenv("AGP_VERSION")
        return if (agpVersion == null) {
            agpVersions.map(Version::parse)
        } else {
            listOf(Version.parse(agpVersion))
        }
    }

    // Checks if a AGP version (receiver) is compatible KSP
    protected fun Version.agpIsCompatibleWithKsp(): Boolean {
        return baseVersion() >= Version.parse("4.1.0")
    }
}

class LanguageTestVersions : ArgumentsProvider, TestVersions() {

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
        val arguments = mutableListOf<Arguments>()
        super.provideArguments(context).forEach { argument ->
            val (gradleVersion, agpVersion) = argument.get()
            arguments += Arguments.of(gradleVersion, agpVersion, JavaProjectSpec())
            arguments += Arguments.of(gradleVersion, agpVersion, KotlinKaptProjectSpec())
            if (Version.parse(agpVersion as String).agpIsCompatibleWithKsp()) {
                arguments += Arguments.of(gradleVersion, agpVersion, KotlinKspProjectSpec())
            }
        }
        return arguments.stream()
    }
}

class NoSchemaLocationTestVersions : ArgumentsProvider, TestVersions() {

    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
        val arguments = mutableListOf<Arguments>()
        super.provideArguments(context).forEach { argument ->
            val (gradleVersion, agpVersion) = argument.get()
            arguments += Arguments.of(gradleVersion, agpVersion, NoSchemaLocationJavaProjectSpec())
            arguments += Arguments.of(gradleVersion, agpVersion, NoSchemaLocationKotlinKaptProjectSpec())
            if (Version.parse(agpVersion as String).agpIsCompatibleWithKsp()) {
                arguments += Arguments.of(gradleVersion, agpVersion, NoSchemaLocationKotlinKspProjectSpec())
            }
        }
        return arguments.stream()
    }
}
