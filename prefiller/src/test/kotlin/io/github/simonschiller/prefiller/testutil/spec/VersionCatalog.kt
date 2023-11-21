package io.github.simonschiller.prefiller.testutil.spec

import io.github.simonschiller.prefiller.testutil.GradleVersionCompatibility

data class VersionCatalog(
    val gradleVersion: String = GradleVersionCompatibility.DEFAULT_GRADLE_VERSION,
    val kotlinVersion: String =  Versions.KOTLIN,
    val agpVersion: String = Versions.AGP,
    val compileSdk: String = Versions.COMPILE_SDK.toString(),
    val minSdk: String = Versions.MIN_SDK.toString(),
    val targetSdk: String = Versions.TARGET_SDK.toString(),
    val kspVersion: String = Versions.KSP,
    val roomCompilerVersion: String = Versions.ROOM,
    val roomRuntimeVersion: String = Versions.ROOM,
) {
    val kotlinGradlePlugin get() = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    val kotlinStdlib get() = "org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}"
    val kspGradlePlugin get() = "com.google.devtools.ksp:symbol-processing-gradle-plugin:$kspVersion"
    val roomCompiler get() = "androidx.room:room-compiler:$roomCompilerVersion"
    val roomRuntime get() = "androidx.room:room-runtime:$roomRuntimeVersion"

    override fun toString(): String {
        return "VC(gradle $gradleVersion, agp $agpVersion, …)"
    }
}
