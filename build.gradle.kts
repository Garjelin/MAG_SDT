import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.20"
    id("org.jetbrains.compose") version "1.5.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "org.example.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "BookShop"
            packageVersion = "1.0.0"
            
            windows {
                // Настройки для Windows
                menuGroup = "Книжный магазин"
                upgradeUuid = "BF6C3E1D-7A3F-4F3E-9B2A-1A5D7C8E9F2B"
                iconFile.set(project.file("src/main/resources/icon.ico"))
            }
            
            description = "Система управления книжным магазином"
            vendor = "Курсовой проект"
            licenseFile.set(project.file("LICENSE.txt"))
        }
    }
}

// Используем JVM 21 (максимум для Kotlin 1.9.20)
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}