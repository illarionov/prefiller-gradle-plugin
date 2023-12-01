package io.github.simonschiller.prefiller.internal

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.Variant
import com.google.devtools.ksp.gradle.KspExtension
import io.github.simonschiller.prefiller.DatabaseConfig
import io.github.simonschiller.prefiller.PrefillerTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import java.util.Locale

internal class PrefillerTaskRegisterer(
    private val project: Project,
    private val variant: Variant,
    private val prefillerClasspath: FileCollection,
) {
    private val extensions: ExtensionContainer = project.extensions
    private val providerFactory: ProviderFactory = project.providers

    fun registerTasks(config: DatabaseConfig) {
        val assets = variant.sources.assets ?: return
        val variantName = variant.name.capitalizeAscii()
        val databaseName = config.name.capitalizeAscii()
        val taskName = "prefill$databaseName${variantName}Database"

        // Validate configuration
        val classname = config.classname.orNull ?: error("No classname configured for database ${config.name}")
        val schemaLocation = getSchemaLocation(config.schemaDirectory)

        val prefillerTask = PrefillerTask.register(
            project = project,
            taskName = taskName,
            databaseName = config.name,
            scriptFiles = config.getScriptFiles(project),
            schemaDirectory = schemaLocation.map { parentDir -> parentDir.dir(classname) },
            variantName = variantName,
            prefillerClasspath = prefillerClasspath,
        )
        assets.addGeneratedSourceDirectory(prefillerTask, PrefillerTask::outputDirectory)

        // Room schema has to be generated before the Prefiller task runs
        val roomGeneratorTaskNames = setOf(
            "compile${variantName}JavaWithJavac",
            "kapt${variantName}Kotlin",
            "ksp${variantName}Kotlin",
        )
        val schemaTasks = project.tasks.matching { task ->
            task.name in roomGeneratorTaskNames || task.name.startsWith("copyRoomSchemas")
        }
        prefillerTask.configure {
            it.dependsOn(schemaTasks)
        }
    }

    // Read the Room schema location from the annotation processor options
    private fun getSchemaLocation(defaultSchemaDir: Provider<Directory>): Provider<Directory> {
        val projectDirectory = project.layout.projectDirectory
        val schemaDirectoryProvider = providerFactory.provider {
            val kaptSchemaLocation = getKaptSchemaLocation()
            val kspSchemaLocation = getKspSchemaLocation()
            val javaAptSchemaLocation = getJavaAptSchemaLocation()

            val schemaLocation = when {
                kaptSchemaLocation != null -> kaptSchemaLocation
                kspSchemaLocation != null -> kspSchemaLocation
                javaAptSchemaLocation != null -> javaAptSchemaLocation
                else -> error("Could not find schema location")
            }
            projectDirectory.dir(schemaLocation)
        }

        return defaultSchemaDir.orElse(schemaDirectoryProvider)
    }

    private fun getKaptSchemaLocation(): String? = try {
        val kaptExtension = extensions.findByType(KaptExtension::class.java)
        val androidExtension = extensions.findByType(CommonExtension::class.java)
        val arguments = kaptExtension?.getAdditionalArguments(project, variant, androidExtension)
        arguments?.get(SCHEMA_LOCATION_KEY)
    } catch (exception: NoClassDefFoundError) {
        null // KAPT plugin not applied
    }

    private fun getKspSchemaLocation(): String? = try {
        extensions.findByType(KspExtension::class.java)?.arguments?.get(SCHEMA_LOCATION_KEY)
    } catch (exception: NoClassDefFoundError) {
        null // KSP plugin not applied
    }

    private fun getJavaAptSchemaLocation(): String? {
        val arguments = variant.javaCompilation.annotationProcessor.arguments.get()
        return arguments[SCHEMA_LOCATION_KEY]
    }

    companion object {
        private const val SCHEMA_LOCATION_KEY = "room.schemaLocation"

        @Suppress("DEPRECATION")
        private fun String.capitalizeAscii(): String = capitalize(Locale.ROOT)

        private fun DatabaseConfig.getScriptFiles(
            project: Project,
        ): ConfigurableFileCollection {
            return if (script.isPresent) {
                project.logger.warn("Deprecated 'script' property was used, please use 'scripts' instead.")
                project.files(script)
            } else {
                scripts
            }
        }
    }
}