package io.github.simonschiller.prefiller.testutil.compatibility

import Versions
import io.github.simonschiller.prefiller.internal.util.Version

internal object KspVersionCompatibility {
    fun hasCompatibleKspVersion(
        kotlinVersion: Version,
    ): Boolean = getKotlinKspVersion(kotlinVersion) != null

    fun getKotlinKspVersion(
        kotlinVersion: Version,
    ): Version? = when {
        kotlinVersion >= Version(1, 9, 20) -> Version(1, 9, 20, "1.0.14")
        kotlinVersion >= Version(1, 9, 10) -> Version(1, 9, 10, "1.0.13")
        kotlinVersion >= Version(1, 9, 0) -> Version(1, 9, 0, "1.0.13")
        kotlinVersion >= Version(1, 8, 22) -> Version(1, 8, 22, "1.0.11")
        kotlinVersion >= Version(1, 8, 21) -> Version(1, 8, 21, "1.0.11")
        kotlinVersion >= Version(1, 8, 20) -> Version(1, 8, 20, "1.0.11")
        kotlinVersion >= Version(1, 8, 10) -> Version(1, 8, 10, "1.0.9")
        kotlinVersion >= Version(1, 8, 0) -> Version(1, 8, 0, "1.0.9")
        kotlinVersion >= Version(1, 7, 22) -> Version(1, 7, 22, "1.0.8")
        kotlinVersion >= Version(1, 7, 21) -> Version(1, 7, 21, "1.0.8")
        kotlinVersion >= Version(1, 7, 20) -> Version(1, 7, 20, "1.0.8")
        kotlinVersion >= Version(1, 7, 10) -> Version(1, 7, 10, "1.0.6")
        kotlinVersion >= Version(1, 7, 0) -> Version(1, 7, 0, "1.0.6")
        else -> null
    }

}
