package io.github.simonschiller.prefiller.testutil.compatibility

import Versions
import io.github.simonschiller.prefiller.internal.util.Version

internal object RoomVersionCompatibility {
    fun getCompatibleRoomVersion(
        compileSdkVersion: Int,
        jvmVersion: Int = Runtime.version().version()[0],
    ): Version {
        // Room 2.6.0 requires JDK 17 (https://issuetracker.google.com/issues/311218683) and version 34 of the
        // Android APIs
        return if (jvmVersion >= 17 && compileSdkVersion >= 34) {
            Version.parse(Versions.ROOM)
        } else {
            Version.parse("2.5.2")
        }
    }
}
