plugins {
    kotlin("jvm") version "2.2.20"
    id("com.google.cloud.tools.jib") version "3.4.5"
}

group = "me.centralhardware"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val ktgbotapiVersion = "28.0.2"

dependencies {
    implementation("dev.inmo:tgbotapi:$ktgbotapiVersion")
    implementation("com.github.centralhardware:ktgbotapi-commons:$ktgbotapiVersion")
    implementation("org.json:json:20250517")
    implementation("io.github.crackthecodeabhi:kreds:0.9.1")
    implementation("org.postgresql:postgresql:42.7.8")
    implementation("com.github.seratch:kotliquery:1.9.1")
}

tasks.test {
    useJUnitPlatform()
}

jib {
    from {
        image = System.getenv("JIB_FROM_IMAGE") ?: "eclipse-temurin:24-jre"
    }
    to {
    }
    container {
        mainClass = "me.centralhardware.znatoki.studyRussianBot.MainKt"
        jvmFlags = listOf("-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0")
        creationTime = "USE_CURRENT_TIMESTAMP"
        labels = mapOf(
            "org.opencontainers.image.source" to (System.getenv("GITHUB_SERVER_URL")?.let { server ->
                val repo = System.getenv("GITHUB_REPOSITORY")
                if (repo != null) "$server/$repo" else ""
            } ?: ""),
            "org.opencontainers.image.revision" to (System.getenv("GITHUB_SHA") ?: "")
        )
        user = "10001"
    }
}