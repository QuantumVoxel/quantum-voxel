import dev.ultreon.gameutils.GameUtilsExt
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.ShortenCommandLine
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import org.jreleaser.gradle.plugin.JReleaserExtension
import org.jreleaser.model.Active
import org.jreleaser.model.api.deploy.maven.MavenCentralMavenDeployer.Stage
import org.jreleaser.model.api.signing.Signing
import java.lang.System.getenv
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//file:noinspection GroovyUnusedCatchParameter
buildscript {
    repositories {
        mavenCentral()

        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            name = "sonatype"
        }

        maven {
            url = uri("https://maven.atlassian.com/3rdparty/")
        }

        maven {
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }

        google()
    }

    dependencies {
        classpath("gradle.plugin.org.danilopianini:javadoc.io-linker:0.1.4-700fdb6")
    }
}

//*****************//
//     Plugins     //
//*****************//
plugins {
    id("idea")
    id("maven-publish")
    id("java")
    id("application")
    id("org.jreleaser") version "1.14.0" apply false
    id("io.freefair.javadoc-links") version "8.3"
}

apply(plugin = "gameutils")

val gameVersion = property("project_version")?.toString() ?: throw IllegalStateException("project_version is not set")
//val gameVersion = "0.1.0+edge." + DateTimeFormatter.ofPattern("yyyy.w.W").format(LocalDateTime.now())
val ghBuildNumber: String? = getenv("GH_BUILD_NUMBER")
version = gameVersion


println("Current version: $gameVersion")
if (ghBuildNumber != null) println("Build number: $ghBuildNumber")

//********************************//
// Setting up the main properties //
//********************************//
extensions.configure<GameUtilsExt> {
    projectName = "Quantum Voxel"

    projectVersion =
        if (ghBuildNumber != null) "$gameVersion+build.$ghBuildNumber"
        else gameVersion
    projectGroup = "dev.ultreon.quantum"
    projectId = "quantum"
    production = true

    coreProject = project(":client")
    desktopProject = project(":desktop")
    packageProject = project(":launcher")

    mainClass = "dev.ultreon.quantum.premain.PreMain"
    javaVersion = 21
}

//**********************//
//     Repositories     //
//**********************//
repositories {
    mavenCentral()
    mavenLocal()
    google()
    maven("https://maven.atlassian.com/3rdparty/")
    maven("https://repo1.maven.org/maven2/")
    maven("https://repo.runelite.net/")
    maven("https://repo.glaremasters.me/repository/public/")
    maven("https://jitpack.io") {
        content {
            includeGroup("dev.ultreon")
            includeGroup("com.github.mgsx-dev.gdx-gltf")
            includeGroup("com.github.JnCrMx")
            includeGroup("com.github.jagrosh")
            includeGroup("space.earlygrey")
        }
    }
    flatDir {
        name = "Project Libraries"
        dirs = setOf(file("${projectDir}/libs"))
    }
}

/*****************
 * Configurations
 */
beforeEvaluate {
    configurations {
        // configuration that holds jars to include in the jar
        getByName("implementation") {
            isCanBeResolved = true
        }
        create("include") {
            isCanBeResolved = true
        }
        create("addToJar") {
            isCanBeResolved = true
        }
    }

    /***************
     * Dependencies
     */
    dependencies {
        configurations["implementation"](project(":teavm"))
    }
}

allprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "java")

    java.sourceCompatibility = JavaVersion.VERSION_21
    java.targetCompatibility = JavaVersion.VERSION_21

    ext.also {
        it["app_name"] = "Quantum Voxel"
        it["gdx_version"] = property("gdx_version")
        it["robo_vm_version"] = "2.3.16"
        it["box_2d_lights_version"] = "1.5"
        it["ashley_version"] = "1.7.4"
        it["ai_version"] = "1.8.2"
        it["gdx_controllers_version"] = "2.2.3"
    }

    repositories {
        // Ultreon Maven Repository
        maven("https://gitlab.com/api/v4/groups/9962021/-/packages/maven")
        
        mavenLocal()
        mavenCentral()
        google()
        maven("https://maven.fabricmc.net/")
        maven("https://oss.sonatype.org/content/repositories/releases")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://repo.glaremasters.me/repository/public/")
        maven("https://jitpack.io") {
            content {
                includeGroup("dev.ultreon")
                includeGroup("dev.ultreon.quantum-fabric-loader")
                includeGroup("com.github.mgsx-dev.gdx-gltf")
                includeGroup("com.github.JnCrMx")
                includeGroup("com.github.jagrosh")
                includeGroup("com.github.crykn.guacamole")
                includeGroup("com.github.Ultreon")
                includeGroup("com.github.Dgzt")
                includeGroup("space.earlygrey")
            }
        }

        flatDir {
            name = "Project Libraries"
            dirs = setOf(file("${projectDir}/libs"))
        }

        flatDir {
            name = "Project Libraries"
            dirs = setOf(file("${rootProject.projectDir}/libs"))
        }
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:23.0.0")

        implementation(annotationProcessor("org.projectlombok:lombok:1.18.34")!!)
        annotationProcessor("com.google.code.findbugs:jsr305:3.0.2")
        annotationProcessor("org.ow2.asm:asm-tree:9.3")
    }
}

subprojects {
    tasks.withType(JavaCompile::class.java).configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType(Jar::class.java).configureEach {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        archiveBaseName.set("quantum-${project.name}")
    }
}

tasks.withType(ProcessResources::class.java).configureEach {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType(Jar::class.java).configureEach {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
}
println("Java: " + System.getProperty("java.version") + " JVM: " + System.getProperty("java.vm.version') + '(' + System.getProperty('java.vendor') + ') Arch: ' + System.getProperty('os.arch"))
println("OS: " + System.getProperty("os.name") + " Version: " + System.getProperty("os.version"))

println("Current version: $gameVersion")
println("Project: $group:$name")

afterEvaluate {
//    tasks.getByName("javadoc", Javadoc::class) {
//        source(subprojects.map { subproject ->
//            if (subproject.name == "android") return@map null
//            subproject?.extensions?.getByType(JavaPluginExtension::class.java)?.sourceSets?.getByName("main")?.allJava?.sourceDirectories
//        }.filterNotNull())
//        this.title = "Quantum Voxel API"
//        this.setDestinationDir(File(rootProject.projectDir, "/build/docs/javadoc"))
//        // Configure the classpath
//        classpath = files(subprojects.map { subproject ->
//            if (subproject.name == "android") return@map null
//            subproject?.extensions?.getByType(JavaPluginExtension::class.java)?.sourceSets?.getByName("main")?.compileClasspath
//        }.filterNotNull())
//        val options = options as StandardJavadocDocletOptions
//        options.addFileOption("-add-stylesheet", project.file("javadoc.css"))
//        options.links(
//            "https://javadoc.io/doc/com.badlogicgames.gdx/gdx/${project.property("gdx_version")}",
//            "https://javadoc.io/doc/com.badlogicgames.gdx/gdx-ai/${project.property("ai_version")}",
//            "https://javadoc.io/doc/com.badlogicgames.gdx/gdx-backend-lwjgl3/${project.property("gdx_version")}",
//            "https://javadoc.io/doc/io.netty/netty-buffer/${project.property("netty_version")}",
//            "https://javadoc.io/doc/io.netty/netty-codec-socks/${project.property("netty_version")}",
//            "https://javadoc.io/doc/io.netty/netty-common/${project.property("netty_version")}",
//            "https://javadoc.io/doc/io.netty/netty-handler/${project.property("netty_version")}",
//            "https://javadoc.io/doc/io.netty/netty-resolver/${project.property("netty_version")}",
//            "https://javadoc.io/doc/io.netty/netty-transport/${project.property("netty_version")}",
//            "https://javadoc.io/doc/io.netty/netty-transport-classes-epoll/${project.property("netty_version")}",
//            "https://javadoc.io/doc/it.unimi.dsi/fastutil/8.5.12",
//            "https://javadoc.io/doc/it.unimi.dsi/fastutil-core/8.5.9",
//            "https://javadoc.io/doc/net.java.dev.jna/jna/${project.property("jna_version")}",
//            "https://javadoc.io/doc/org.apache.logging.log4j/log4j-api/${project.property("log4j_version")}",
//            "https://javadoc.io/doc/org.apache.logging.log4j/log4j-core/${project.property("log4j_version")}",
//            "https://javadoc.io/doc/org.apache.commons/commons-collections4/${project.property("commons_collections4_version")}",
//            "https://javadoc.io/doc/org.apache.commons/commons-compress/${project.property("commons_compress_version")}",
//            "https://javadoc.io/doc/org.apache.commons/commons-lang3/${project.property("commons_lang3_version")}",
//            "https://javadoc.io/doc/org.slf4j/slf4j-api/${project.property("slf4j_version_javadoc")}",
//            "https://ultreon.github.io/corelibs/docs/${project.property("corelibs_version")}",
//            "https://ultreon.github.io/ultreon-data/docs/${project.property("ultreon_data_version")}",
//            "https://maven.fabricmc.net/docs/fabric-loader-${project.property("fabric_version")}",
//        )
//    }
//
//    this.apply(plugin = "org.danilopianini.javadoc.io-linker")
}

artifacts {

}

val publishProjects =
    listOf(project(":client"), project(":desktop"), project(":launcher"), project(":server"), project(":gameprovider"), project(":mixinprovider"), project(":lang-gen"))

publishProjects.forEach {
    if (it.name == "android") return@forEach
    it.mkdir("build/jreleaser")
    it.apply(plugin = "maven-publish")
    it.apply(plugin = "java-library")
    it.apply(plugin = "org.jreleaser")

    it.extensions.configure<JReleaserExtension>("jreleaser") {
        project {
            group = "dev.ultreon.quantum"
            authors.set(listOf("XyperCode"))
            license = "Ultreon-PSL-1.0"
            description = "Quantum Voxel is a voxel game that focuses on technology based survival (currently in development)."
            copyright.set("(C) Copyright 2023 Quinten Jungblut. All rights reserved.")
            links {
                homepage = "https://ultreon.dev"
            }
            inceptionYear = "2023"
        }

        gitRootSearch = true

        release {
            gitlab {
                branch.set("dev")
            }
        }

        signing {
            active.set(Active.ALWAYS)
            armored = true
        }

        deploy {
            maven {
                this@maven.active.set(Active.ALWAYS)

//                nexus2 {
//                    create("maven-central") {
//                        active.set(Active.SNAPSHOT)
//                        url.set("https://s01.oss.sonatype.org/service/local")
//                        snapshotUrl.set("https://s01.oss.sonatype.org/content/repositories/snapshots/")
//                        closeRepository = true
//                        releaseRepository = true
//                        stagingRepository("build/staging-deploy")
//                    }
//                }

                mavenCentral {
                    create("sonatype") {
                        active.set(Active.ALWAYS)
                        url.set("https://central.sonatype.com/api/v1/publisher")
                        stagingRepository(projectDir.path + "/build/staging-deploy")
                    }
                }
            }
        }
    }

    it.publishing {
        repositories {
            maven {
                it.mkdir("build/staging-deploy")
                name = "Staging"
                url = uri("file://${projectDir.path.replace("\\", "/")}/build/staging-deploy")
            }
        }

        publications {
            create<MavenPublication>("mavenJava") {
                from(it.components["java"])

                groupId = "dev.ultreon.quantum"
                artifactId = "quantum-${it.name}"
                version = gameVersion

                pom {
                    this@pom.name.set("QuantumVoxel")
                    this@pom.description.set("Quantum Voxel is a voxel game that focuses on technology based survival.")

                    this@pom.url.set("https://github.com/Ultreon/quantum-voxel")
                    this@pom.inceptionYear.set("2023")

                    this@pom.developers {
                        developer {
                            id.set("XyperCode")
                            name.set("XyperCode")

                            organization.set("Ultreon")
                            organizationUrl.set("https://ultreon.github.io/")

                            roles.set(listOf("Owner", "Maintainer"))

                            url.set("https://github.com/Ultreon/quantum-voxel")
                        }
                    }

                    this@pom.organization {
                        name.set("Ultreon")
                        url.set("https://ultreon.github.io/")
                    }

                    this@pom.issueManagement {
                        system.set("GitHub")
                        url.set("https://github.com/Ultreon/quantum-voxel/issues")
                    }

                    this@pom.scm {
                        url.set("https://github.com/Ultreon/quantum-voxel")
                        connection.set("scm:git:git://github.com/Ultreon/quantum.git")
                    }

                    this@pom.licenses {
                        license {
                            name.set("AGPL-3.0")
                            url.set("https://github.com/Ultreon/quantum-voxel/blob/main/LICENSE")
                        }
                    }

                    this@pom.contributors {
                        contributor {
                            name.set("XyperCode")
                            url.set("https://github.com/XyperCode")

                            roles.set(listOf("Author", "Developer"))
                        }

                        contributor {
                            name.set("MaroonShaded")
                            url.set("https://github.com/MaroonShaded")

                            roles.set(listOf("Contributor"))
                        }

                        contributor {
                            name.set("Oszoou")
                            url.set("https://github.com/Oszoou")

                            roles.set(listOf("Contributor"))
                        }

                        contributor {
                            name.set("MincraftEinstein")
                            url.set("https://github.com/MincraftEinstein")

                            roles.set(listOf("Tester", "Modeler"))
                        }
                    }
                }
            }
        }
    }
}

tasks.register<Copy>("docker-jar") {
    dependsOn(":server:jar")

    from(project(":server").tasks.getByName("serverJar").outputs)
    into("$projectDir/build/docker")

    rename {
        "server.jar"
    }
}

tasks.register<DefaultTask>("docker-prepare") {
    dependsOn("docker-jar")

    doLast {
        // Prepare docker image
        val runScriptPath = file("$projectDir/build/docker/run.sh").toPath()

        val serverClassPath = project(":server").configurations["runtimeClasspath"]!!
        val classPath = serverClassPath.asSequence()
            .filter { it != null }
            .map {
                copy {
                    from(it)
                    into("$projectDir/build/docker/lib")
                }
                return@map "./lib/${it.name}"
            }
            .joinToString(":")

        val runScript = """
#!/bin/bash
java -cp ./server.jar:$classPath net.fabricmc.loader.impl.launch.knot.KnotClient
        """.trimIndent()
        if (!Files.exists(runScriptPath))
            Files.createFile(runScriptPath)

        Files.writeString(runScriptPath, runScript, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)

        copy {
            from("$projectDir/log4j.xml")
            into("$projectDir/build/docker")
        }

        copy {
            from("$projectDir/Dockerfile")
            into("$projectDir/build/docker")
        }

        Files.writeString(
            Paths.get("$projectDir/build/docker/image-version.txt"),
            gameVersion,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.CREATE
        )
    }
}

tasks.register<DefaultTask>("docker-image") {
    dependsOn("docker-prepare")

    doLast {
        // Build docker image
        exec {
            workingDir = file("$projectDir/build/docker")
            if (!gameVersion.matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+"))) {
                commandLine("docker", "build", "-t", "${project.name}/server:latest", ".")
            } else {
                commandLine("docker", "build", "-t", "${project.name}/server:${gameVersion}", ".")
            }
        }
    }
}

tasks.register<DefaultTask>("docker-run") {
    dependsOn("docker-image")

    doLast {
        exec {
            workingDir = file("$projectDir/build/docker")
            commandLine("docker", "run", "--rm", "-i", "--name", "gradle-${project.name}", "${project.name}/server")
        }
    }
}

tasks.register<DefaultTask>("docker-push") {
    dependsOn("docker-image")

    doLast {
        exec {
            workingDir = file("$projectDir/build/docker")

            // Push docker image
            if (!gameVersion.matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+")))
                commandLine(
                    "docker", "build",
                    "--label",
                    "org.opencontainers.image.description=A WIP voxel game that aims to have a lot of features",

                    "--label",
                    "org.opencontainers.image.url=https://github.com/Ultreon/quantum-voxel",

                    "--label",
                    "org.opencontainers.image.source=https://github.com/Ultreon/quantum-voxel",

                    "--label",
                    "org.opencontainers.image.version=latest",

                    "--label",
                    "org.opencontainers.image.created=${
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    }",

                    "--label",
                    "org.opencontainers.image.revision=${getenv("GH_SHA")}",

                    "--label",
                    "org.opencontainers.image.licenses=AGPL-3.0",

                    "--tag",
                    "ghcr.io/ultreon/${project.name}:server-latest", "."
                )
            else
                commandLine(
                    "docker", "build",
                    "--label",
                    "org.opencontainers.image.description=A WIP voxel game that aims to have a lot of features",

                    "--label",
                    "org.opencontainers.image.url=https://github.com/Ultreon/quantum-voxel",

                    "--label",
                    "org.opencontainers.image.source=https://github.com/Ultreon/quantum-voxel",

                    "--label",
                    "org.opencontainers.image.version=${gameVersion}",

                    "--label",
                    "org.opencontainers.image.created=${
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    }",

                    "--label",
                    "org.opencontainers.image.revision=${getenv("GH_SHA")}",

                    "--label",
                    "org.opencontainers.image.licenses=AGPL-3.0",

                    "--tag",
                    "ghcr.io/ultreon/${project.name}:server-${gameVersion}", "."
                )

            isIgnoreExitValue = false
        }

        exec {
            workingDir = file("$projectDir/build/docker")

            if (!gameVersion.matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+"))) {
                commandLine("docker", "push", "ghcr.io/ultreon/${project.name}:server-latest")
            } else {
                commandLine("docker", "push", "ghcr.io/ultreon/${project.name}:server-${gameVersion}")
            }
        }
    }
}

apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")
idea {
    project {
        settings {
            fun setupIdea(
                dependency: Project,
                name: String,
                env: String,
                knotEnv: String,
                workingDir: String = dependency.file("run").absolutePath
            ) {
                dependency.afterEvaluate {
                    mkdir(workingDir)
                    Files.createDirectories(Paths.get(dependency.projectDir.path, "build", "gameutils"))

                    val ps = File.pathSeparator!!
                    val clientFiles = dependency.configurations["runtimeClasspath"]!!.files

                    val fs = File.separator!!

                    val clientClassPath = clientFiles.asSequence()
                        .filter { it != null }
                        .map { it.path }
                        .joinToString(ps)

                    val serverFiles = dependency.configurations["runtimeClasspath"]!!.files

                    val serverClassPath = serverFiles.asSequence()
                        .filter { it != null }
                        .map { it.path }
                        .joinToString(ps)

                    val conf = """
commonProperties
  fabric.development=true
  log4j2.formatMsgNoLookups=true
  fabric.log.disableAnsi=false
  fabric.skipMcProvider=true
  fabric.zipfs.use_temp_file=false
  fabric.gameJarPath.client=$clientClassPath
  fabric.gameJarPath.server=$serverClassPath
  log4j.configurationFile=${rootProject.projectDir}${fs}log4j.xml
    """.trimIndent()
                    val launchFile = file("${dependency.projectDir}/build/gameutils/launch.cfg")
                    Files.writeString(
                        launchFile.toPath(),
                        conf,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                    )

                    val cpFile = file("${dependency.projectDir}/build/gameutils/classpath.txt")
                    Files.writeString(
                        cpFile.toPath(),
                        clientClassPath,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                    )

                    var defaultJvmArgs = "-Xmx4g -Dfabric.dli.config=\"${launchFile.path}\""

                    if (System.getProperty("os.name").lowercase().startsWith("mac")) {
                        defaultJvmArgs += " -XstartOnFirstThread"
                    }

                    withIDEADir {
                        println("Callback 1 executed with: $absolutePath")
                    }

                    runConfigurations {
                        create(
                            "Quantum $name",
                            Application::class.java
                        ) {
                            jvmArgs =
                                "$defaultJvmArgs -Dfabric.dli.env=$env -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.Knot$knotEnv"
                            mainClass = "net.fabricmc.devlaunchinjector.Main"
                            moduleName = rootProject.name + ".${dependency.name}.main"
                            workingDirectory = workingDir
                            programParameters = "--gameDir=."
                            shortenCommandLine = ShortenCommandLine.ARGS_FILE
                        }
                    }
                }

            }

            setupIdea(rootProject.project(":desktop"), "Client", "CLIENT", "Client", "$projectDir/run/client/main/")
            setupIdea(rootProject.project(":desktop"), "Client Alt", "CLIENT", "Client", "$projectDir/run/client/alt/")
            setupIdea(rootProject.project(":server"), "Server", "SERVER", "Server", "$projectDir/run/server/")
        }
    }
}

tasks.register<Exec>("runClient") {
    workingDir = file("$projectDir/run/client/")
    Files.createDirectories(Paths.get(workingDir.path))

    dependsOn(":desktop:build")

    val classpath = project(":desktop").sourceSets["main"].runtimeClasspath.files.joinToString(File.pathSeparator)

    val argFile = File.createTempFile("argfile", ".args").apply {
        deleteOnExit()
        writeText(
            """
            -Xmx4g
            -Xms4g
            -Dfabric.dli.env=CLIENT
            -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient
            -Dfabric.development=true
            -Dlog4j2.formatMsgNoLookups=true
            -Dfabric.log.disableAnsi=false
            -Dfabric.skipMcProvider=true
            -Dfabric.zipfs.use_temp_file=false
            -Dlog4j.configurationFile=${rootProject.projectDir}/log4j.xml
            -cp
            $classpath
            net.fabricmc.devlaunchinjector.Main
            --gameDir=.
            """.trimIndent()
        )
    }

    commandLine(
        Path.of(System.getProperty("java.home")).toAbsolutePath().resolve("bin").resolve(
            if (System.getProperty("os.name").lowercase().startsWith("mac")) "java"
            else if (System.getProperty("os.name").lowercase().startsWith("win")) "java.exe"
            else "java"
        ),
        "@${argFile.path}"
    )
}

tasks.register<Exec>("runClientAlt") {
    workingDir = file("$projectDir/run/data/")
    Files.createDirectories(Paths.get("$projectDir/run/data/"))

    dependsOn(":desktop:build")

    val classpath = project(":desktop").sourceSets["main"].runtimeClasspath.files.joinToString(File.pathSeparator)

    val argFile = File.createTempFile("argfile", ".args").apply {
        deleteOnExit()
        writeText(
            """
            -Xmx4g
            -Xms4g
            -Dfabric.dli.env=CLIENT
            -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient
            -Dfabric.development=true
            -Dlog4j2.formatMsgNoLookups=true
            -Dfabric.log.disableAnsi=false
            -Dfabric.skipMcProvider=true
            -Dfabric.zipfs.use_temp_file=false
            -Dlog4j.configurationFile=${rootProject.projectDir}/log4j.xml
            -cp
            $classpath
            net.fabricmc.devlaunchinjector.Main
            --gameDir=.
            """.trimIndent()
        )
    }

    commandLine(
        Path.of(System.getProperty("java.home")).toAbsolutePath().resolve("bin").resolve(
            if (System.getProperty("os.name").lowercase().startsWith("mac")) "java"
            else if (System.getProperty("os.name").lowercase().startsWith("win")) "java.exe"
            else "java"
        ).toString(),
        "@${argFile.absolutePath}"
    )

    group = "runs"
}

tasks.register<Exec>("runDataGenClient") {
    workingDir = file("$projectDir/run/data/")
    Files.createDirectories(Paths.get("$projectDir/run/data/"))

    dependsOn(":desktop:build")

    val classpath = project(":desktop").sourceSets["main"].runtimeClasspath.files.joinToString(File.pathSeparator)

    val argFile = File.createTempFile("argfile", ".args").apply {
        deleteOnExit()
        writeText(
            """
            -Xmx4g
            -Xms4g
            -Dfabric.dli.env=CLIENT
            -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient
            -Dfabric.development=true
            -Dlog4j2.formatMsgNoLookups=true
            -Dfabric.log.disableAnsi=false
            -Dfabric.skipMcProvider=true
            -Dfabric.zipfs.use_temp_file=false
            -Dlog4j.configurationFile=${rootProject.projectDir}/log4j.xml
            -Dquantum.datagen=true
            -Dquantum.datagen.path=${project(":client").projectDir}/src/main/datagen
            -cp
            $classpath
            net.fabricmc.devlaunchinjector.Main
            --gameDir=.
            """.trimIndent()
        )
    }

    commandLine(
        Path.of(System.getProperty("java.home")).toAbsolutePath().resolve("bin").resolve(
            if (System.getProperty("os.name").lowercase().startsWith("mac")) "java"
            else if (System.getProperty("os.name").lowercase().startsWith("win")) "java.exe"
            else "java"
        ).toString(),
        "@${argFile.absolutePath}"
    )

    group = "runs"
}

tasks.register<Exec>("runDataGenServer") {
    workingDir = file("$projectDir/run/data/")
    Files.createDirectories(Paths.get("$projectDir/run/data/"))

    dependsOn(":server:build")

    val classpath = project(":server").sourceSets["main"].runtimeClasspath.files.joinToString(File.pathSeparator)

    val argFile = File.createTempFile("argfile", ".args").apply {
        deleteOnExit()
        writeText(
            """
            -Xmx4g
            -Xms4g
            -Dfabric.dli.env=SERVER
            -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotServer
            -Dfabric.development=true
            -Dlog4j2.formatMsgNoLookups=true
            -Dfabric.log.disableAnsi=false
            -Dfabric.skipMcProvider=true
            -Dfabric.zipfs.use_temp_file=false
            -Dlog4j.configurationFile=${rootProject.projectDir}/log4j.xml
            -Dquantum.datagen=true
            -Dquantum.datagen.path=${project(":server").projectDir}/src/main/datagen
            -cp
            $classpath
            net.fabricmc.devlaunchinjector.Main
            --gameDir=.
            """.trimIndent()
        )
    }

    commandLine(
        Path.of(System.getProperty("java.home")).toAbsolutePath().resolve("bin").resolve(
            if (System.getProperty("os.name").lowercase().startsWith("mac")) "java"
            else if (System.getProperty("os.name").lowercase().startsWith("win")) "java.exe"
            else "java"
        ).toString(),
        "@${argFile.absolutePath}"
    )
}

tasks.register<Exec>("runServer") {
    workingDir = file("$projectDir/run/server/")
    Files.createDirectories(Paths.get("$projectDir/run/server/"))

    dependsOn(":server:build")

    val classpath = project(":server").sourceSets["main"].runtimeClasspath.files.joinToString(File.pathSeparator)

    val argFile = File.createTempFile("argfile", ".args").apply {
        deleteOnExit()
        writeText(
            """
            -Xmx4g
            -Xms4g
            -Dfabric.dli.env=SERVER
            -Dfabric.dli.config=
            -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotServer
            -Dfabric.development=true
            -Dlog4j2.formatMsgNoLookups=true
            -Dfabric.log.disableAnsi=false
            -Dfabric.skipMcProvider=true
            -Dfabric.zipfs.use_temp_file=false
            -Dlog4j.configurationFile=${rootProject.projectDir}/log4j.xml
            -cp
            $classpath
            net.fabricmc.devlaunchinjector.Main
            --gameDir=.
            """.trimIndent()
        )
    }

    commandLine(
        Path.of(System.getProperty("java.home")).toAbsolutePath().resolve("bin").resolve(
            if (System.getProperty("os.name").lowercase().startsWith("mac")) "java"
            else if (System.getProperty("os.name").lowercase().startsWith("win")) "java.exe"
            else "java"
        ).toString(),
        "@${argFile.absolutePath}"
    )
}
