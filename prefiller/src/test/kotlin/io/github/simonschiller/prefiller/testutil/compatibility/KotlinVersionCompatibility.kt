package io.github.simonschiller.prefiller.testutil.compatibility

import Versions
import io.github.simonschiller.prefiller.internal.util.Version

internal object KotlinVersionCompatibility {

    fun hasCompatibleKotlinVersion(
        agpVersion: Version,
        gradleVersion: Version,
    ): Boolean = getKotlinCompatibleVersion(agpVersion, gradleVersion) != null

    // https://kotlinlang.org/docs/gradle-configure-project.html#apply-the-plugin
    fun getKotlinCompatibleVersion(
        agpVersion: Version,
        gradleVersion: Version,
    ): Version? = when {
        gradleVersion >= Version(6, 8, 3) -> {
            Version.parse(Versions.KOTLIN)
        }

        gradleVersion >= Version(6, 7, 1) -> {
            if (agpVersion <= Version(7, 0, 4)) {
                Version(1, 7, 22)
            } else {
                null
            }
        }

        gradleVersion >= Version(6, 1, 1) -> {
            if (agpVersion <= Version(7, 0, 2)) {
                Version(1, 7, 10)
            } else {
                null
            }
        }

        else -> null
    }
}
