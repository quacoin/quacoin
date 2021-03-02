import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
}

group = "com.kraskaska"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}
val ktor_version = "1.5.2"
dependencies {
//    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-mustache:$ktor_version")
    implementation("ch.qos.logback:logback-classic:1.2.3")
}
kotlin {
    sourceSets["main"].apply {
        kotlin.srcDir("src/main/kotlin")
        resources.srcDir("src/main/resources")
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}