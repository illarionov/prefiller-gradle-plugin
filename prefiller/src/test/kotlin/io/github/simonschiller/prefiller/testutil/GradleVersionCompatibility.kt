package io.github.simonschiller.prefiller.testutil

import io.github.simonschiller.prefiller.internal.util.Version

internal object GradleVersionCompatibility {
    // Checks if a Gradle version can run on the current JVM
    fun gradleIsCompatibleWithRuntime(gradleVersion: Version): Boolean {
        val jvmVersion = Runtime.version().version()[0]
        return gradleVersion >= getMinimumGradleVersionOnJvm(jvmVersion)
    }

    // https://docs.gradle.org/current/userguide/compatibility.html#java
    private fun getMinimumGradleVersionOnJvm(jvmVersion: Int): Version = if (jvmVersion >= 8) {
        when (jvmVersion) {
            8 -> Version(2, 0)
            9 -> Version(4, 3)
            10 -> Version(4, 7)
            11 -> Version(5, 0)
            12 -> Version(5, 4)
            13 -> Version(6, 0)
            14 -> Version(6, 3)
            15 -> Version(6, 7)
            16 -> Version(7, 0)
            17 -> Version(7, 3)
            18 -> Version(7, 5)
            19 -> Version(7, 6)
            20 -> Version(8, 3)
            21 -> Version(8, 5)
            else -> Version(8, 5)
        }
    } else {
        Version(1, 0)
    }
}
