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
    // Default entry point — override with: ./gradlew run -PmainClass=<qualified class name>
    // Examples:
    //   ./gradlew run -PmainClass=assessment1.Problem1Kt
    //   ./gradlew run -PmainClass=assessment3.Problem2Kt
    //   ./gradlew run -PmainClass=airbnb.Problem1Kt
    //   ./gradlew run -PmainClass=coinbase.solutions.BankingSystemKt
    //   ./gradlew run -PmainClass=dropbox.solution.FileSystemSolutionKt
    //   ./gradlew run -PmainClass=warmups.WarmUpsKt
    mainClass.set(project.findProperty("mainClass") as String? ?: "assessment1.Problem1Kt")
}
