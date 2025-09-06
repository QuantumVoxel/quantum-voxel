plugins {
  id("java")
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

  implementation("io.github.monstroussoftware.gdx-webgpu:gdx-webgpu:0.2")
  implementation("com.github.xpenatan.jWebGPU:webgpu-core:0.1.4")
}

tasks.test {
  useJUnitPlatform()
}