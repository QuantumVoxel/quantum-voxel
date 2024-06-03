plugins {

}

group = "dev.ultreon.quantum"
version = "0.1.0+snapshot.2024.05.31.00.04"

repositories {
    mavenCentral()
}

tasks.register<Zip>("dist") {
    archiveFileName.set("js-api.jsz")
    destinationDirectory.set(file("$projectDir/build/dist"))

    from(fileTree("$projectDir/src/main/js/"))
}
