plugins {
    kotlin("jvm") version "2.0.20"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    // https://mvnrepository.com/artifact/cglib/cglib
    implementation("cglib:cglib:3.3.0")
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.assertj:assertj-core:3.18.1")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
//tasks.test {
//    useJUnitPlatform()
//}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}
