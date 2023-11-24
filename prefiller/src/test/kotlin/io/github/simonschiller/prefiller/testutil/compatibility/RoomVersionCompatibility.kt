package io.github.simonschiller.prefiller.testutil.compatibility

import io.github.simonschiller.prefiller.internal.util.Version
import io.github.simonschiller.prefiller.testutil.spec.VersionCatalog

internal object RoomVersionCompatibility {
    val ROOM_2_4_3 = Version(2, 4, 3)
    val ROOM_2_5_2 = Version(2, 5, 2)
    val ROOM_2_6_0 = Version(2, 6, 0)

    fun getCompatibleAndroidxCoreRuntimeVersion(
        compileSdkVersion: Int,
    ): Version {
        return if (compileSdkVersion >= 33) {
            Version.parse(Versions.CORE_RUNTIME)
        } else {
            Version(2, 1, 0)
        }
    }

    fun getCompatibleRoomVersion(
        compileSdkVersion: Int,
        jvmVersion: Int = Runtime.version().version()[0],
    ): Version = when {
        compileSdkVersion >= 34 -> {
            if (jvmVersion >= 17) {
                // Room 2.6.0 requires JDK 17 (https://issuetracker.google.com/issues/311218683) and version 34
                // of the Android APIs
                Version.parse(Versions.ROOM)
            } else {
                ROOM_2_5_2
            }
        }

        compileSdkVersion >= 33 -> ROOM_2_5_2
        else -> ROOM_2_4_3
    }

    fun VersionCatalog.isCompatibleWithRoomGradlePlugin(): Boolean {
        return Version.parse(roomRuntimeVersion) >= ROOM_2_6_0
    }
}
