import java.nio.file.Files
import java.nio.file.StandardOpenOption

plugins {
    id("idea")
    id("java")
}

group = "dev.ultreon.quantummods"
version = "0.1.0"

base {
    archivesName.set("testmod")
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(project(":client"))
    implementation(project(":desktop"))
    implementation(project(":server"))
}

tasks.test {
    useJUnitPlatform()
}
