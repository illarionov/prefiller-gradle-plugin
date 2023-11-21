package io.github.simonschiller.prefiller.testutil.compatibility

import Versions
import io.github.simonschiller.prefiller.internal.util.Version

object RoomVersionCompatibility {

    fun getCompatibleRoomVersion(): Version = getCompatibleRoomVersion(
        Runtime.version().version()[0],
    )

    private fun getCompatibleRoomVersion(
        jvmVersion: Int,
    ): Version {
        // https://issuetracker.google.com/issues/311218683
        return if (jvmVersion >= 17) {
            Version.parse(Versions.ROOM)
        } else {
            Version.parse("2.5.2")
        }
    }
}
