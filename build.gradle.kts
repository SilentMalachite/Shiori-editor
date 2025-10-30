import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.5.12"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "com.texteditor"

version = "1.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

// JavaFX プラットフォーム判定（WebView 用）
val javaFxVersion = "21"
val osName = System.getProperty("os.name").lowercase()
val osArch = System.getProperty("os.arch").lowercase()
val javaFxPlatform =
    when {
        osName.contains("mac") && (osArch.contains("aarch64") || osArch.contains("arm")) ->
            "mac-aarch64"
        osName.contains("mac") -> "mac"
        osName.contains("win") -> "win"
        else -> "linux"
    }

dependencies {
    // Compose Desktop
    implementation(compose.desktop.currentOs)

    // FlexMark for Markdown parsing
    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")

    // JavaFX (WebView)
    implementation("org.openjfx:javafx-base:$javaFxVersion:$javaFxPlatform")
    implementation("org.openjfx:javafx-graphics:$javaFxVersion:$javaFxPlatform")
    implementation("org.openjfx:javafx-controls:$javaFxVersion:$javaFxPlatform")
    implementation("org.openjfx:javafx-web:$javaFxVersion:$javaFxPlatform")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.test { useJUnitPlatform() }

compose.desktop {
    application {
        mainClass = "com.texteditor.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Shiori Editor"
            packageVersion = "1.0.0"

            macOS {
                val iconPath = project.file("src/main/resources/icon.icns")
                if (iconPath.exists()) {
                    iconFile.set(iconPath)
                }
            }
            windows {
                val iconPath = project.file("src/main/resources/icon.ico")
                if (iconPath.exists()) {
                    iconFile.set(iconPath)
                }
            }
            linux {
                val iconPath = project.file("src/main/resources/icon.png")
                if (iconPath.exists()) {
                    iconFile.set(iconPath)
                }
            }
        }
    }
}

spotless {
    kotlin {
        ktfmt("0.46").kotlinlangStyle()
        target("src/**/*.kt")
        targetExclude("build/**/*.kt")
    }

    kotlinGradle { ktfmt("0.46").kotlinlangStyle() }
}

tasks.register("format") {
    group = "formatting"
    description = "Format all Kotlin code"
    dependsOn("spotlessApply")
}

tasks.register("checkFormat") {
    group = "verification"
    description = "Check Kotlin code formatting"
    dependsOn("spotlessCheck")
}
