plugins {
    id("java")
    id("com.gradleup.shadow") version("9.3.2")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "com.artillexstudios.axminions"
version = "2.0.0"

repositories {
    mavenCentral()

    maven("https://redempt.dev/")
    maven("https://repo.artillex-studios.com/releases/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation("com.artillexstudios.axapi:axapi:2.0.4:all")
    implementation("com.h2database:h2:2.3.232")
//    compileOnly("me.kryniowesegryderiusz:kgenerators-core:7.3") {
//        exclude("com.github.WaterArchery", "LitMinionsAPI")
//        exclude("com.github.Slimefun", "Slimefun4")
//        exclude("com.bgsoftware", "WildStackerAPI")
//    }
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.apache.commons:commons-lang3:3.14.0")
    compileOnly("dev.triumphteam:triumph-gui:3.1.10")
    compileOnly("com.github.Redempt:Crunch:2.0.3")
    compileOnly("commons-io:commons-io:2.16.1")
    compileOnly("it.unimi.dsi:fastutil:8.5.13")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }

    shadowJar {
        relocate("com.artillexstudios.axapi", "com.artillexstudios.axminions.axapi")
        relocate("dev.jorel.commandapi", "com.artillexstudios.axminions.commandapi")
        relocate("redempt.crunch", "com.artillexstudios.axminions.crunch")
        relocate("org.h2", "com.artillexstudios.axminions.h2")
    }

    runServer {
        minecraftVersion("1.21.11")
    }
}