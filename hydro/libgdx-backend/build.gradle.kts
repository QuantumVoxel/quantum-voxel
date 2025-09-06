plugins {
  id("java")
  id("java-library")
}

group = "dev.ultreon.quantum"
version = "0.2.0-alpha.2"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.13.4"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  implementation(project(":hydro"))

  implementation("com.badlogicgames.gdx:gdx:1.13.5")
}

tasks.test {
  useJUnitPlatform()
}