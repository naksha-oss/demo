plugins {
    java
    application
    kotlin("jvm") version "2.1.20"
}

repositories {
    //mavenLocal()
    mavenCentral()
}

kotlin {
    jvmToolchain {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_23)
        }
    }
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_23)
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