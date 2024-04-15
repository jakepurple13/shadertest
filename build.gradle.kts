import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.FileInputStream
import java.util.*

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.programmersbox"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://jitpack.io")
    maven("https://maven.pkg.github.com/Qawaz/compose-code-editor") {
        name = "GitHubPackages"
        credentials {
            val githubProperties = Properties()
            githubProperties.load(FileInputStream(rootProject.file("local.properties")))
            username = githubProperties["gpr.user"] as String? ?: System.getenv("USERNAME")
            password = githubProperties["gpr.key"] as String? ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("com.wakaztahir:codeeditor:3.0.5")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "shadertest"
            packageVersion = "1.0.0"
        }
    }
}
