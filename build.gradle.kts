plugins {
    kotlin("jvm") version "2.3.10"
}

group = "org.soyuz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val lwjglV = "3.3.3"
val jomlV ="1.10.5"
val lwjglNatives = "natives-windows"

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.lwjgl:lwjgl:$lwjglV")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglV")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglV")
    implementation("org.lwjgl:lwjgl-stb:$lwjglV")
    implementation("org.joml:joml:$jomlV")

    implementation("org.lwjgl:lwjgl::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-glfw::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-opengl::$lwjglNatives")
    implementation("org.lwjgl:lwjgl-stb::$lwjglNatives")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}