plugins {
  id("java")
  id("java-library")
}

group = "dev.ultreon.hydro"
version = "0.1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.13.4"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  api("org.joml:joml:1.10.8")
}

tasks.test {
  useJUnitPlatform()
}