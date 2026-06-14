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
    mainClass.set("org.soyuz.MainKt")
}

val lwjglV = "3.3.3"
val lwjglNatives = "natives-windows"

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.lwjgl:lwjgl:$lwjglV")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglV")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglV")
    implementation("org.lwjgl:lwjgl-stb:$lwjglV")

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

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.soyuz.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}