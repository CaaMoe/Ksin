import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import java.text.SimpleDateFormat
import java.util.*

buildscript {
    dependencies {
        classpath(libs.kotlinx.serialization.json)
    }
}

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin
    id("com.gradleup.shadow") version libs.versions.shadow

    id("xyz.jpenilla.run-velocity") version libs.versions.run.velocity
}

group = "moe.caa"
version = "1.0.0+${getGitCommitID().substring(0, 8)}"

configurations.register("extra")

repositories {
    file("config/repositories.txt").readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .forEach { maven(it) }
}

dependencies {
    compileOnly(libs.velocity.api)

    compileOnlyAndExtra(libs.kotlin.stdlib)
    compileOnlyAndExtra(libs.kotlin.reflect)

    compileOnlyAndExtra(libs.bstats.velocity)
    compileOnlyAndExtra(libs.bstats.base)
    compileOnlyAndExtra(libs.hikaricp)

    compileOnlyAndExtra(libs.exposed.core)
    compileOnlyAndExtra(libs.exposed.jdbc)
    compileOnlyAndExtra(libs.exposed.migration.core)
    compileOnlyAndExtra(libs.exposed.migration.jdbc)
}

fun DependencyHandler.compileOnlyAndExtra(dependencyNotation: Any) {
    add("compileOnly", dependencyNotation)
    add("extra", dependencyNotation)
}

tasks.build { finalizedBy(tasks.shadowJar) }
tasks.shadowJar {
    configurations = emptyList()

    file("config/relocations.txt").readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .forEach {
            val parts = it.split("\\s+".toRegex())
            if (parts.size != 2) {
                throw IllegalArgumentException("Invalid relocation entry: $it")
            }
            relocate(parts[0], parts[1])
        }

    archiveFileName.set("Ksin.jar")
    manifest {
        attributes["Built-By"] = System.getProperty("user.name")
        attributes["Build-Jdk"] = System.getProperty("java.version")
        attributes["Build-OS"] =
            "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}"
        attributes["Build-Timestamp"] = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date())
        attributes["Build-Revision"] = getGitCommitID()
        attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
    }
}

tasks.jar { enabled = false }

tasks.processResources {
    from("${project.rootDir}/config/relocations.txt") { into("") }
    from("${project.rootDir}/config/repositories.txt") { into("") }

    doLast {
        destinationDir.mkdirs()
        File(destinationDir, "dependencies.txt").apply {
            writeText(
                configurations["extra"].resolvedConfiguration.firstLevelModuleDependencies.joinToString("\n") {
                    "${it.moduleGroup}:${it.moduleName}:${it.moduleVersion}"
                }
            )
        }
        File(destinationDir, "velocity-plugin.json").apply {
            writeText(
                buildJsonObject {
                    put("id", JsonPrimitive("ksin"))
                    put("name", JsonPrimitive("Ksin"))
                    put("version", JsonPrimitive(project.version.toString()))
                    put("authors", buildJsonArray {
                        project.projectDir.resolve("config").resolve("contributors.txt").takeIf {
                            it.exists()
                        }?.readLines()?.filter {
                            it.isNotEmpty()
                        }?.forEach {
                            add(JsonPrimitive(it))
                        }
                    })
                    put("description", JsonPrimitive("Bad signature skin repairer."))
                    put("main", JsonPrimitive("moe.caa.multilogin.ksin.internal.bootstrap.KsinBootstrap"))
                }.toString()
            )
        }
    }
}

tasks {
    runVelocity {
        velocityVersion("3.4.0-SNAPSHOT")
    }
}

fun Project.getGitCommitID(): String = runCatching {
    ProcessBuilder("git", "rev-parse", "HEAD")
        .directory(project.projectDir)
        .start()
        .inputStream
        .bufferedReader()
        .readText()
        .trim()
}.getOrElse { "unknown" }