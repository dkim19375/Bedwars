@file:Suppress("SpellCheckingInspection")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("java")
    id("me.bristermitten.pdm") version "0.0.33"
    id("org.jetbrains.kotlin.jvm") version "1.4.32"
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
        // relocate("de.tr7zw.changeme.nbtapi", "me.dkim19375.bedwars.nbtapi")
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
    maven("https://repo.codemc.org/repository/maven-public/")
}

subprojects {
    apply {
        plugin("java")
    }
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }
    apply {
        plugin("me.bristermitten.pdm")
    }
    apply {
        plugin("com.github.johnrengelman.shadow")
    }

    group = parent!!.group
    version = parent!!.version

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    repositories {
        // mavenLocal()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://jitpack.io")
        maven("https://repo.dmulloy2.net/nexus/repository/public/")
        maven("https://repo.mattstudios.me/artifactory/public")
        maven("https://repo.codemc.org/repository/maven-public/")
        flatDir {
            dirs("libs")
        }
    }

    dependencies {
        pdm("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "1.4.32")
        implementation("com.github.dkim19375", "dkim19375Core", "2.6.0")
        pdm("me.mattstudios.utils", "matt-framework-gui", "2.0.3.3")
        pdm("net.kyori", "adventure-api", "4.7.0")
        pdm("net.kyori", "adventure-text-serializer-legacy", "4.7.0")
        // implementation("de.tr7zw:item-nbt-api:2.7.1")
        compileOnly("de.tr7zw", "item-nbt-api-plugin", "2.7.1")
        compileOnly("com.comphenix.protocol", "ProtocolLib", "4.6.0")
        compileOnly("org.jetbrains", "annotations", "20.1.0")
        implementation(fileTree("../libs"))
        // https://github.com/katsumag/ItemActionsLib
        // https://github.com/ThatKawaiiSam/Assemble
    }
}

dependencies {
    for (project in subprojects) {
        implementation(project)
    }
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