import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.ncorti.ktfmt.gradle") version "0.20.1"
}

group = "me.centralhardware"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("dev.inmo:tgbotapi:18.2.2")
    implementation("com.github.centralhardware:telegram-bot-commons:1aec284aa3")
    implementation("org.json:json:20240303")
    implementation("io.github.crackthecodeabhi:kreds:0.9.1")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.github.seratch:kotliquery:1.9.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("shadow")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "me.centralhardware.znatoki.studyRussianBot.MainKt"))
        }
    }
}

ktfmt {
    kotlinLangStyle()
}