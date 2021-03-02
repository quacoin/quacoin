import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
}

group = "com.kraskaska"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}
kotlin {
    sourceSets {
        val main by getting {

            dependencies {
                implementation("io.ktor:ktor-kotlinMultiplatform:1.5.1")
            }
        }
    }
    sourceSets["main"].apply {
        kotlin.srcDir("src/main/kotlin/")
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}