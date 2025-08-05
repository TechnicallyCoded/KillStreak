plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-rc1"
}

group = "com.tcoded"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.tcoded.com/releases")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.5")
    implementation("com.tcoded:FoliaLib:0.5.1")
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.withType<JavaCompile> {
    options.release.set(17)
}

tasks.jar {
    archiveClassifier = "raw"
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("com.tcoded.folialib", "com.tcoded.killstreak.folialib")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
