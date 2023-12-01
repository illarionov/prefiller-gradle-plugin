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

package io.github.simonschiller.prefiller

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.DynamicFeaturePlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestPlugin
import io.github.simonschiller.prefiller.internal.PrefillerTaskRegisterer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage

class PrefillerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("prefiller", PrefillerExtension::class.java)

        // Eagerly fail the build if the AGP is not available
        if (!project.plugins.hasPlugin("com.android.internal.version-check")) {
            error("Prefiller is only applicable to Android projects")
        }

        val prefillerClasspath = project.configurations.maybeCreate("prefiller").apply {
            description = "All Prefiller dependencies"
            isCanBeResolved = true
            isVisible = false
            isCanBeConsumed = false
            attributes {
                it.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
            }
            defaultDependencies { dependencies ->
                PREFILLER_RUNTIME_DEPENDENCIES.forEach {
                    dependencies.add(project.dependencies.create(it))
                }
            }
        }

        listOf(
            AppPlugin::class.java,
            LibraryPlugin::class.java,
            DynamicFeaturePlugin::class.java,
            TestPlugin::class.java,
        ).forEach { agpLibraryPlugin ->
            project.plugins.withType(agpLibraryPlugin) {
                project.extensions.configure(AndroidComponentsExtension::class.java) { agpExtension ->
                    agpExtension.onVariants { variant ->
                        val registerer = PrefillerTaskRegisterer(project, variant, prefillerClasspath)
                        extension.databaseConfigs.forEach(registerer::registerTasks)
                    }
                }
            }
        }
    }
}
