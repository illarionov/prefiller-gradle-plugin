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

package io.github.simonschiller.prefiller.testutil.spec

open class KotlinKspProjectSpec(
    override val versionCatalog: VersionCatalog,
) : KotlinProjectSpec() {

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
                classpath("io.github.simonschiller:prefiller:+")
	        }
        }

    """.trimIndent()

    override fun getModuleBuildGradleContent() = """
        apply plugin: "com.android.application"
        apply plugin: "kotlin-android"
        apply plugin: "com.google.devtools.ksp"
        apply plugin: "io.github.simonschiller.prefiller"

        repositories {
            google()
            mavenCentral()
        }
        android {
            compileSdkVersion(${versionCatalog.compileSdk})
        	defaultConfig {
            	minSdkVersion(${versionCatalog.minSdk})
            	targetSdkVersion(${versionCatalog.targetSdk})
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
            ksp {
                arg("room.schemaLocation", projectDir.absolutePath + "/schemas")
            }
        }
        dependencies {
            implementation("${versionCatalog.kotlinStdlib}")
            implementation("${versionCatalog.roomRuntime}")
            ksp("${versionCatalog.roomCompiler}")
        }
        prefiller {
            database("people") {
                classname.set("com.test.PeopleDatabase")
                scripts.from(file("setup.sql"))
            }
        }

    """.trimIndent()

    override fun toString() = "Kotlin project using KSP"
}
