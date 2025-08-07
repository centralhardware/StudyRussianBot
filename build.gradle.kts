plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "me.centralhardware"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("me.centralhardware.znatoki.studyRussianBot.MainKt")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val ktgbotapiVersion = "27.1.2"

dependencies {
    implementation("dev.inmo:tgbotapi:$ktgbotapiVersion")
    implementation("com.github.centralhardware:ktgbotapi-commons:$ktgbotapiVersion-1")
    implementation("org.json:json:20250517")
    implementation("io.github.crackthecodeabhi:kreds:0.9.1")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.github.seratch:kotliquery:1.9.1")
}

tasks.test {
    useJUnitPlatform()
}
