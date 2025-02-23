plugins {
    id("java")
    id("com.gradleup.shadow") version("9.0.0-beta8")
}

group = "com.artillexstudios.axminions"
version = "2.0.0"

repositories {
    mavenCentral()

    maven("https://redempt.dev/")
    maven("https://repo.artillex-studios.com/releases/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation("com.artillexstudios.axapi:axapi:1.4.547:all")
    implementation("dev.jorel:commandapi-bukkit-shade:9.7.0")
    implementation("com.h2database:h2:2.3.232")
//    compileOnly("me.kryniowesegryderiusz:kgenerators-core:7.3") {
//        exclude("com.github.WaterArchery", "LitMinionsAPI")
//        exclude("com.github.Slimefun", "Slimefun4")
//        exclude("com.bgsoftware", "WildStackerAPI")
//    }
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
    compileOnly("org.apache.commons:commons-lang3:3.14.0")
    compileOnly("dev.triumphteam:triumph-gui:3.1.10")
    compileOnly("com.github.Redempt:Crunch:2.0.3")
    compileOnly("commons-io:commons-io:2.16.1")
    compileOnly("it.unimi.dsi:fastutil:8.5.13")
    compileOnly("org.slf4j:slf4j-api:2.0.9")
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly("org.jooq:jooq:3.19.10")
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
        relocate("com.zaxxer", "com.artillexstudios.axminions.hikaricp")
        relocate("org.jooq", "com.artillexstudios.axminions.jooq")
        relocate("org.h2", "com.artillexstudios.axminions.h2")
    }
}