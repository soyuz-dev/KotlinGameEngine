
import org.gradle.internal.os.OperatingSystem
plugins {
    kotlin("jvm") version "2.3.10"
    application
}

group = "org.soyuz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.soyuz.AVTestKt")
}

val lwjglV = "3.3.3"
val lwjglNatives = when {
    OperatingSystem.current().isWindows -> "natives-windows"
    OperatingSystem.current().isLinux -> "natives-linux"
    OperatingSystem.current().isMacOsX -> {
        // Handle Intel (x64) vs Apple Silicon (arm64) Macs
        if (System.getProperty("os.arch").startsWith("aarch64")) "natives-macos-arm64" else "natives-macos"
    }
    else -> throw GradleException("Unsupported operating system")
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.lwjgl:lwjgl:$lwjglV")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglV")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglV")
    implementation("org.lwjgl:lwjgl-stb:$lwjglV")
    implementation("org.lwjgl:lwjgl-openal:${lwjglV}")

    runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-openal::$lwjglNatives")



}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.soyuz.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}