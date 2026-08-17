import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    application
    kotlin("jvm") version "2.4.10"
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvmToolchain {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
    sourceSets {
        main {

        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.bundles.naksha.libs)
    implementation(libs.bundles.naksha.impl)
}

application {
    mainClass.set("MainKt")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}