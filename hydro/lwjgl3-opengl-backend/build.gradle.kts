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

  api(project(":hydro"))

  //noinspection NewerVersionAvailable
  {

    api("org.lwjgl:lwjgl:3.3.3")
    api("org.lwjgl:lwjgl-glfw:3.3.3")
    api("org.lwjgl:lwjgl-opengl:3.3.3")
    api("org.lwjgl:lwjgl-openal:3.3.3")
    api("org.lwjgl:lwjgl-opencl:3.3.3")
    api("org.lwjgl:lwjgl-stb:3.3.3")

    // Windows-specific native bindings
    runtimeOnly("org.lwjgl:lwjgl:3.3.3:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-glfw:3.3.3:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl:3.3.3:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-openal:3.3.3:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-stb:3.3.3:natives-windows")

    // Linux-specific native bindings
    runtimeOnly("org.lwjgl:lwjgl:3.3.3:natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-glfw:3.3.3:natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-opengl:3.3.3:natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-openal:3.3.3:natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-stb:3.3.3:natives-linux")

    // macOS-specific native bindings
    runtimeOnly("org.lwjgl:lwjgl:3.3.3:natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-glfw:3.3.3:natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-opengl:3.3.3:natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-openal:3.3.3:natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-stb:3.3.3:natives-macos")
  }
}

tasks.test {
  useJUnitPlatform()
}