package io.github.simonschiller.prefiller.testutil.spec

open class KotlinKspRoomPluginProjectSpec(
    versionCatalog: VersionCatalog,
) : KotlinProjectSpec(versionCatalog) {

    override fun getRootBuildGradleContent() = """
        buildscript {
            repositories {
                mavenLocal()
                google()
		        mavenCentral()
	        }
	        dependencies {
		        classpath("com.android.tools.build:gradle:${versionCatalog.agpVersion}")
                classpath("${versionCatalog.kotlinGradlePlugin}")
                classpath("${versionCatalog.kspGradlePlugin}")
                classpath("${versionCatalog.roomGradlePlugin}")
                classpath("io.github.simonschiller:prefiller:+")
	        }
        }

    """.trimIndent()

    override fun getModuleBuildGradleContent() = """
        plugins {
            id "com.android.application"
            id "org.jetbrains.kotlin.android"
            id "com.google.devtools.ksp"
            id "androidx.room"
            id "io.github.simonschiller.prefiller"
        }

        repositories {
            google()
            mavenCentral()
        }
        android {
            compileSdkVersion(${versionCatalog.compileSdk})
            ${getNamespaceContent()}
        	defaultConfig {
            	minSdkVersion(${versionCatalog.minSdk})
            	targetSdkVersion(${versionCatalog.targetSdk})
                javaCompileOptions {
                    annotationProcessorOptions {
                        arguments += ["room.generateKotlin": "true"]
                    }
                }
            }
            compileOptions {
                sourceCompatibility = JavaVersion.${versionCatalog.compatibilityJavaVersion.name}
                targetCompatibility = JavaVersion.${versionCatalog.compatibilityJavaVersion.name}
            }
            kotlinOptions {
                jvmTarget = "${versionCatalog.compatibilityJavaVersion}"
            }
        }
        dependencies {
            implementation("${versionCatalog.androidxCoreRuntime}")
            implementation("${versionCatalog.kotlinStdlib}")
            implementation("${versionCatalog.roomRuntime}")
            ksp("${versionCatalog.roomCompiler}")
        }
        def schemaDir = layout.projectDirectory.dir("schemas")
        room {
            schemaDirectory(provider { schemaDir.asFile.path })
        }
        prefiller {
            database("people") {
                schemaDirectory.set(schemaDir)
                classname.set("com.test.PeopleDatabase")
                scripts.from(file("setup.sql"))
            }
        }

    """.trimIndent()

    override fun toString() = "Kotlin project using KSP and Room plugin ($versionCatalog)"
}
