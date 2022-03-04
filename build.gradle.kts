plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("java")
}

group = "net.islandearth"
version = "1.1.4"

repositories {
    mavenCentral()

    maven("https://erethon.de/repo/")
    maven("https://repo.convallyria.com/snapshots")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.opencollab.dev/maven-snapshots/")

    maven {
        name = "codemc-snapshots"
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }

     flatDir { dirs("libraries") } // FastLogin
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    compileOnly("org.spigotmc:spigot-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.0-SNAPSHOT")
    compileOnly("fr.xephi:authme:5.6.0-SNAPSHOT")
    compileOnly(":FastLoginBukkit")

    implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT") // anvilgui
    implementation("net.islandearth:languagy:2.0.4-SNAPSHOT") // languagy
}

tasks {
    test {
        useJUnitPlatform()
    }

    shadowJar {
        relocate("net.islandearth.languagy", "net.islandearth.anvillogin.libs.languagy")
        relocate("net.wesjd.anvilgui", "net.islandearth.anvillogin.libs.anvilgui")

        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}