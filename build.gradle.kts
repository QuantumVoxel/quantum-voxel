import dev.ultreon.gameutils.GameUtilsExt
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import java.lang.System.getenv
import java.nio.file.Files
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
//        classpath("com.android.tools.build:gradle:8.2.0")
    }
}

//*****************//
//     Plugins     //
//*****************//
plugins {
    id("idea")
    id("maven-publish")
    id("java")
    id("io.freefair.javadoc-links") version "8.3"
}

apply(plugin = "gameutils")


//val gameVersion = "0.1.0"
val gameVersion = "0.1.0-edge." + DateTimeFormatter.ofPattern("yyyy.w.W").format(LocalDateTime.now()) + ".1"
val ghBuildNumber: String? = getenv("GH_BUILD_NUMBER")

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
    packageProject = project(":desktop-merge")

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
                includeGroup("com.github.mgsx-dev.gdx-gltf")
                includeGroup("com.github.JnCrMx")
                includeGroup("com.github.jagrosh")
                includeGroup("com.github.crykn.guacamole")
                includeGroup("com.github.Ultreon")
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
    listOf(project(":client"), project(":desktop"), project(":server"), project(":gameprovider"))

publishProjects.forEach {
    if (it.name == "android") return@forEach
    it.apply(plugin = "maven-publish")
    it.apply(plugin = "java-library")

    it.publishing {
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

            repositories {
                maven {
                    name = "GitLabMaven"
                    url = uri("https://gitlab.com/api/v4/projects/59634919/packages/maven")
                    credentials(HttpHeaderCredentials::class) {
                        name = "Private-Token"
                        value =
                            findProperty("gitLabPrivateToken") as String? // the variable resides in $GRADLE_USER_HOME/gradle.properties
                    }
                    authentication {
                        create("header", HttpHeaderAuthentication::class)
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

        Files.writeString(Paths.get("$projectDir/build/docker/image-version.txt"), gameVersion, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
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
                commandLine("docker", "build",
                    "--label",
                    "org.opencontainers.image.description=A WIP voxel game that aims to have a lot of features",

                    "--label",
                    "org.opencontainers.image.url=https://github.com/Ultreon/quantum-voxel",

                    "--label",
                    "org.opencontainers.image.source=https://github.com/Ultreon/quantum-voxel",

                    "--label",
                    "org.opencontainers.image.version=latest",

                    "--label",
                    "org.opencontainers.image.created=${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",

                    "--label",
                    "org.opencontainers.image.revision=${getenv("GH_SHA")}",

                    "--label",
                    "org.opencontainers.image.licenses=AGPL-3.0",

                    "--tag",
                    "ghcr.io/ultreon/${project.name}:server-latest", ".")
            else
                commandLine("docker", "build",
                    "--label",
                    "org.opencontainers.image.description=A WIP voxel game that aims to have a lot of features",

                    "--label",
                    "org.opencontainers.image.url=https://github.com/Ultreon/quantum-voxel",

                    "--label",
                    "org.opencontainers.image.source=https://github.com/Ultreon/quantum-voxel",

                    "--label",
                    "org.opencontainers.image.version=${gameVersion}",

                    "--label",
                    "org.opencontainers.image.created=${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",

                    "--label",
                    "org.opencontainers.image.revision=${getenv("GH_SHA")}",

                    "--label",
                    "org.opencontainers.image.licenses=AGPL-3.0",

                    "--tag",
                    "ghcr.io/ultreon/${project.name}:server-${gameVersion}", ".")

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
            fun setupIdea(dependency: Project, name: String) {
                dependency.afterEvaluate {
                    mkdir("${dependency.projectDir}/build/gameutils")
                    mkdir("${dependency.projectDir}/run")
                    mkdir("${dependency.projectDir}/run/client")
                    mkdir("${dependency.projectDir}/run/client/alt")
                    mkdir("${dependency.projectDir}/run/client/main")
                    mkdir("${dependency.projectDir}/run/server")

                    val ps = File.pathSeparator!!
                            val files = dependency.configurations["runtimeClasspath"]!!.files

                    val classPath = files.asSequence()
                            .filter { it != null }
                            .map { it.path }
                            .joinToString(ps)

//language=TEXT
                    val conf = """
commonProperties
  fabric.development=true
  log4j2.formatMsgNoLookups=true
  fabric.log.disableAnsi=false
  fabric.skipMcProvider=true
  fabric.zipfs.use_temp_file=false
  log4j.configurationFile=${rootProject.projectDir}/log4j.xml
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
                            classPath,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    )

                    var defaultJvmArgs = "-Xmx4g -Dfabric.dli.config=${launchFile.path}"

                    if (System.getProperty("os.name").lowercase().startsWith("mac")) {
                        defaultJvmArgs += " -XstartOnFirstThread"
                    }

                    withIDEADir {
                        println("Callback 1 executed with: $absolutePath")
                    }

                    runConfigurations {
                        create(
                                "Quantum Voxel Client $name",
                                Application::class.java
                        ) {
                            jvmArgs ="$defaultJvmArgs -Dfabric.dli.env=CLIENT -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient"
                            mainClass = "net.fabricmc.devlaunchinjector.Main"
                            moduleName = rootProject.name + ".${dependency.name}.main"
                            workingDirectory = "$projectDir/run/client/main/"
                            programParameters = "--gameDir=."
                        }
                        create(
                                "Quantum Voxel Data $name",
                                Application::class.java
                        ) {
                                jvmArgs ="$defaultJvmArgs -Dfabric.dli.env=CLIENT -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient"
                            mainClass = "dev.ultreon.quantum.data.gen.DataGenerator"
                            moduleName = rootProject.name + ".desktop.main"
                            workingDirectory = "$projectDir/run/data/main/"
                            programParameters = "--gameDir=."
                        }
                        create(
                                "Quantum Voxel Client $name Alt",
                                Application::class.java
                        ) {
                            jvmArgs ="$defaultJvmArgs -Dfabric.dli.env=CLIENT -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient"
                            mainClass = "net.fabricmc.devlaunchinjector.Main"
                            moduleName = rootProject.name + ".${dependency.name}.main"
                            workingDirectory = "$projectDir/run/client/alt/"
                            programParameters = "--gameDir=."
                        }
                        create(
                                "Quantum Voxel Server $name",
                                Application::class.java
                        ) {
                            jvmArgs ="$defaultJvmArgs -Dfabric.dli.env=SERVER -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotServer"
                            mainClass = "net.fabricmc.devlaunchinjector.Main"
                            moduleName = rootProject.name + ".${dependency.name}.main"
                            workingDirectory = "$projectDir/run/server/"
                            programParameters = "--gameDir=."
                        }
                    }
                }

            }

            setupIdea(rootProject.project(":desktop"), "Desktop")
            setupIdea(rootProject.project(":testmod"), "Test Mod")
        }
    }
}
