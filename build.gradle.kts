plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.6.1")
    testImplementation("io.kotest:kotest-assertions-core:5.6.1")
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
