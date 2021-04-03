import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("java")
    id("me.bristermitten.pdm") version "0.0.33"
    kotlin("jvm") version "1.4.32"
}

group = "me.dkim19375"
version = "1.0-SNAPSHOT"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    named<ShadowJar>("shadowJar") {
        relocate("me.dkim19375.dkim19375core", "me.dkim19375.bedwars.dkim19375core")
        relocate("me.bristermitten.pdm", "me.dkim19375.bedwars.pdm")
        finalizedBy("copyFileToServer")
    }
}

repositories {
    // mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://repo.mattstudios.me/artifactory/public")
}

dependencies {
    pdm("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.32")
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    implementation("com.github.dkim19375:dkim19375Core:2.6.0")
    pdm("me.mattstudios.utils:matt-framework-gui:2.0.3.3")
    pdm("net.kyori:adventure-api:4.7.0")
    pdm("net.kyori:adventure-text-serializer-legacy:4.7.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0")
    implementation(fileTree("libs") { include("*.jar") })
    // https://github.com/katsumag/ItemActionsLib
}

tasks.processResources {
    expand("pluginVersion" to project.version)
}

tasks.register<Copy>("copyFileToServer") {
    File("../.TestServers/1.8/plugins/" + project.name + "-" + project.version + "-all.jar").delete()
    from("build/libs/" + project.name + "-" + project.version + "-all.jar")
    into("../.TestServers/1.8/plugins")
    include("*.jar")
}

tasks {
    build {
        finalizedBy("copyFileToServer")
        dependsOn("pdm")
    }
}