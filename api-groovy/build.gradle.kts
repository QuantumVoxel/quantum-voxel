plugins {
    id("idea")
    id("java")
    id("groovy")
    id("java-library")
    id("scala")
}

apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")

group = "dev.ultreon.quantummods"
version = "0.1-SNAPSHOT"

base {
    archivesName.set("testmod")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    api(project(":api"))
    api(project(":client"))
    api(project(":server"))
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create("mavenScala", MavenPublication::class) {
            //noinspection GrUnresolvedAccess
            from(components["java"])
        }
    }
}
