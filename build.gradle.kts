plugins {
    id("java")
    id("io.github.goooler.shadow") version("8.1.7")
}

group = "com.artillexstudios.axminions"
version = "2.0.0"

repositories {
    mavenCentral()

    maven("https://redempt.dev/")
    maven("https://repo.artillex-studios.com/releases/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation("com.artillexstudios.axapi:axapi:1.4.310:all")
    implementation("dev.jorel:commandapi-bukkit-shade:9.5.0")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.8")
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
    compileOnly("org.apache.commons:commons-lang3:3.14.0")
    implementation("com.github.Redempt:Crunch:2.0.3") // TODO: Load at runtime
    compileOnly("commons-io:commons-io:2.16.1")
    compileOnly("it.unimi.dsi:fastutil:8.5.13")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    implementation("org.jooq:jooq:3.19.10") // TODO: Load at runtime
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
        relocate("org.bstats", "com.artillexstudios.axminions.bstats")
    }
}