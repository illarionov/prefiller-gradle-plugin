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

import com.android.build.gradle.internal.cxx.configure.TaskName
import io.github.simonschiller.prefiller.internal.DatabasePopulator
import io.github.simonschiller.prefiller.internal.RoomSchemaLocator
import io.github.simonschiller.prefiller.internal.parser.StatementParserFactory
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.workers.ClassLoaderWorkerSpec
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
open class PrefillerTask @Inject constructor(
    objectFactory: ObjectFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {
    @InputFiles
    @get:Classpath
    val prefillerClasspath: ConfigurableFileCollection = objectFactory.fileCollection()

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemaDirectory: DirectoryProperty = objectFactory.directoryProperty()

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val scriptFiles: ConfigurableFileCollection = objectFactory.fileCollection()

    @get:Input
    val databaseFileName: Property<String> = objectFactory.property(String::class.java)

    @OutputDirectory
    val outputDirectory: DirectoryProperty = objectFactory.directoryProperty()

    @Internal
    val generatedDatabaseFile: Provider<RegularFile> = outputDirectory.zip(databaseFileName) { dir, fileName ->
        dir.file(fileName)
    }

    @TaskAction
    fun generateDatabase() {
        // Find the latest database schema file
        val schemaLocator = RoomSchemaLocator()
        val schemaFile = schemaLocator.findLatestRoomSchema(schemaDirectory.get().asFile)

        val queue = workerExecutor.classLoaderIsolation { workerSpec: ClassLoaderWorkerSpec ->
            workerSpec.classpath.from(prefillerClasspath)
        }
        queue.submit(GenerateDatabase::class.java) { parameters ->
            parameters.schemaFile.set(schemaFile)
            parameters.scriptFiles.setFrom(scriptFiles)
            parameters.databaseFile.set(generatedDatabaseFile)
        }
    }

    interface GenerateDatabaseWorkParameters : WorkParameters {
        val schemaFile: RegularFileProperty
        val scriptFiles: ConfigurableFileCollection
        val databaseFile: RegularFileProperty
    }

    abstract class GenerateDatabase : WorkAction<GenerateDatabaseWorkParameters> {
        override fun execute() = try {
            val schemaFile = parameters.schemaFile.asFile.get()

            // Parse the statements
            val parserFactory = StatementParserFactory()
            val setupStatements = parserFactory.createParser(schemaFile).parse()
            val scriptStatements = parameters.scriptFiles.flatMap { parserFactory.createParser(it).parse() }

            // Clear the old and populate the new database
            val databaseFile = parameters.databaseFile.asFile.get()
            val databasePopulator = DatabasePopulator()
            databasePopulator.populateDatabase(databaseFile, setupStatements, overwrite = true)
            databasePopulator.populateDatabase(databaseFile, scriptStatements, overwrite = false)
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    internal companion object {
        fun register(
            project: Project,
            taskName: TaskName,
            databaseName: String,
            scriptFiles: ConfigurableFileCollection,
            schemaDirectory: Provider<Directory>,
            variantName: String,
            prefillerClasspath: FileCollection,
            outputDirectory: Provider<Directory>? = null,
        ): TaskProvider<PrefillerTask> = project.tasks.register(taskName, PrefillerTask::class.java) { prefillerTask ->
            prefillerTask.description = "Generates and pre-fills $databaseName database for variant $variantName"
            prefillerTask.prefillerClasspath.from(prefillerClasspath)
            prefillerTask.databaseFileName.set("${databaseName}.db")
            prefillerTask.scriptFiles.setFrom(scriptFiles)
            prefillerTask.schemaDirectory.set(schemaDirectory)
            if (outputDirectory != null) {
                prefillerTask.outputDirectory.set(outputDirectory)
            }
        }
    }
}
