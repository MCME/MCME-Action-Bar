import groovy.json.JsonOutput
import groovy.util.ConfigSlurper
import java.io.File
import javax.imageio.ImageIO

plugins {
    java
    id("com.gradleup.shadow") version "9.3.1"
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("com.diffplug.spotless") version "8.10.3"
}

group = "com.mcmiddleearth"
version = "1.1"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly(files(File(System.getProperty("user.home"), "MCME/dev/jars/VentureChat.jar")))
    compileOnly("net.luckperms:api:5.5")
}

// https://docs.gradle.org/current/dsl/org.gradle.api.plugins.JavaPluginExtension.html
java {
    toolchain {
        // Intellij should pick this up
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// Points git at the repo's hooks so the pre-commit formatter is enabled for everyone who builds
if (file(".git").exists()) {
    providers.exec { commandLine("git", "config", "core.hooksPath", ".githooks") }.result.get()
}

spotless {
    java {
        palantirJavaFormat()
        removeUnusedImports()
    }
}

tasks {
    // Dynamically adding the version to the paper plugin.yml
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    // Replace the normal gradle build with shadowJar
    // prevents the non fat JAR from ever being built
    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier = "" // Removes -all from the end of the generated JAR
    }

    runServer {
        minecraftVersion("26.2")
        downloadPlugins {
            modrinth("luckperms", "v5.5.71-bukkit")
        }
    }
}

tasks.register("generateFontJson") {
    group = "resourcepack"
    description = "Generates default.json from hud-elements.groovy (computes ascents automatically)"

    val resourcePack = file("resourcepack")
    val manifest = resourcePack.resolve("hud-elements.groovy")
    val output = resourcePack.resolve("assets/minecraft/font/default.json")

    inputs.file(manifest)
    outputs.file(output)

    doLast {
        val config = ConfigSlurper().parse(manifest.toURI().toURL())
        val providers = mutableListOf<Map<String, Any>>()
        val negativeSpaces = mutableMapOf<String, Int>()

        // HUD elements — ascent computed from elementId, no manual formula needed
        @Suppress("UNCHECKED_CAST")
        (config["hudElements"] as List<Map<String, Any>>).forEach { element ->
            val elementId = element["elementId"] as Int
            val ascent = -(((elementId + 1024) shl 13) + 4095)
            val height = (element["height"] as Int?) ?: 8
            val advances = mutableSetOf<Int>()
            (element["glyphs"] as List<Map<String, String>>).forEach { glyph ->
                val image = glyph.getValue("image")
                val provider = mutableMapOf<String, Any>(
                    "type" to "bitmap",
                    "file" to image,
                    "ascent" to ascent,
                    "chars" to listOf(glyph.getValue("char")),
                )
                if (element.containsKey("height")) provider["height"] = height
                providers += provider
                advances += glyphAdvance(resourcePack, image, height)
            }

            // Each element is followed by a negative space that cancels its glyph's advance, so the
            // action bar string always has zero width and elements never shift each other
            if (advances.size != 1) {
                throw GradleException("HUD element $elementId: all glyphs must have the same advance, got $advances")
            }
            negativeSpaces[Character.toString(0xF000 + elementId)] = -advances.first()
            println("Element $elementId: advance ${advances.first()}")
        }

        providers += mapOf("type" to "space", "advances" to negativeSpaces)

        output.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(mapOf("providers" to providers))))
        println("Generated $output (${providers.size} providers)")
    }
}

// Mirrors Minecraft's BitmapProvider: the advance is the rightmost non-transparent column,
// scaled to the glyph height, rounded, plus 1px spacing
fun glyphAdvance(resourcePack: File, resourceLocation: String, height: Int): Int {
    val (namespace, path) = resourceLocation.split(":")
    val image = ImageIO.read(resourcePack.resolve("assets/$namespace/textures/$path"))
    val actualWidth = (image.width - 1 downTo 0)
        .firstOrNull { x -> (0 until image.height).any { y -> (image.getRGB(x, y) ushr 24) != 0 } }
        ?.plus(1) ?: 0
    return (0.5 + actualWidth * (height / image.height.toFloat())).toInt() + 1
}
