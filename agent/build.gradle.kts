plugins {
  id("java")
}

group = "dev.ultreon.quantum"
version = "0.2.0-alpha.2"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
  useJUnitPlatform()
}

tasks.jar {
  manifest {
    attributes["Premain-Class"] = "dev.ultreon.quantum.agent.QuantumAgent"
  }
}
