package io.github.simonschiller.prefiller.testutil

import io.github.simonschiller.prefiller.internal.util.Version

internal object AgpVersionCompatibility {
    // Checks if a AGP version (receiver) can run on the current JVM
    fun agpIsCompatibleWithRuntime(agpVersion: Version): Boolean {
        val jvmVersion = Runtime.version().version()[0]
        return when {
            agpVersion >= Version(8, 0) -> jvmVersion >= 17
            agpVersion >= Version(7, 0) -> jvmVersion >= 11
            else -> jvmVersion >= 8
        }
    }

    // Checks if a AGP version [agpVersion] is compatible with a [gradleVersion] version of Gradle
    // See https://developer.android.com/build/releases/past-releases
    fun agpIsCompatibleWithGradle(
        agpVersion: Version,
        gradleVersion: Version,
    ) = when {
        agpVersion >= Version.parse("8.3.0") -> gradleVersion >= Version.parse("8.3")
        agpVersion >= Version.parse("8.2.0") -> gradleVersion >= Version.parse("8.2")
        agpVersion >= Version.parse("8.0.0") -> gradleVersion >= Version.parse("8.0")
        agpVersion >= Version.parse("7.4.0") -> gradleVersion >= Version.parse("7.5")
        agpVersion >= Version.parse("7.3.0") -> gradleVersion >= Version.parse("7.4")
        agpVersion >= Version.parse("7.2.0") -> gradleVersion >= Version.parse("7.3.3")
        agpVersion >= Version.parse("7.1.0") -> gradleVersion >= Version.parse("7.2")
        agpVersion >= Version.parse("7.0.0") -> gradleVersion >= Version.parse("7.0")
        agpVersion >= Version.parse("4.2.0") -> gradleVersion >= Version.parse("6.7.1")
        agpVersion >= Version.parse("4.1.0") -> gradleVersion >= Version.parse("6.5")
        agpVersion >= Version.parse("4.0.0") -> gradleVersion >= Version.parse("6.1.1") && gradleVersion < Version.parse("7.0")

        else -> false
    }

    // Checks if a AGP version is compatible KSP
    fun agpIsCompatibleWithKsp(agpVersion: Version): Boolean {
        return agpVersion.baseVersion() >= Version.parse("4.1.0")
    }
}
