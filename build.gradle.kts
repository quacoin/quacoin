import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.useIR = true
plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
}

group = "com.kraskaska"
version = "0.3-SNAPSHOT"

repositories {
    mavenCentral()
}
val ktor_version = "1.5.2"
dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-mustache:$ktor_version")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.1.0")
    implementation("com.charleskorn.kaml:kaml:0.28.3")
    implementation("org.snakeyaml", "snakeyaml-engine", "2.2.1")
    implementation("no.tornado:tornadofx:1.7.20")
}
kotlin {
    sourceSets["main"].apply {
        kotlin.srcDir("src/main/kotlin")
        resources.srcDir("src/main/resources")
    }
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.withType<org.gradle.jvm.tasks.Jar> {
    manifest {
        attributes["Main-Class"] = "org.quacoin.tayfun.ClientKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}