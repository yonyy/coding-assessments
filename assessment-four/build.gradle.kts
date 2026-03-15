plugins {
    kotlin("jvm") version "2.1.10"
    application
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(25)
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "23"
    targetCompatibility = "23"
}

application {
    // Default entry point — override with: gradle run -PmainClass=Problem2Kt
    mainClass.set(project.findProperty("mainClass") as String? ?: "Problem1Kt")
}
