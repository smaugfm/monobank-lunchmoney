import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.8.10"
    id("org.jlleitschuh.gradle.ktlint") version "11.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    application
}

group = "io.github.smaugfm"
version = "0.0.1-SNAPSHOT"

val javaVersion = "11"

repositories {
    mavenCentral()
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

configure<KtlintExtension> {
    enableExperimentalRules.set(true)
    reporters {
        reporter(ReporterType.HTML)
    }
}

detekt {
    buildUponDefaultConfig = true
    config = files("$projectDir/detekt.yml")
}

tasks {
    withType<DetektCreateBaselineTask> {
        jvmTarget = javaVersion
    }
    withType<Detekt> {
        jvmTarget = javaVersion
        reports {
            html.required.set(true)
            md.required.set(true)
            xml.required.set(false)
            txt.required.set(false)
            sarif.required.set(false)
        }
    }
    withType<Jar> {
        from(rootDir.resolve("LICENSE")) {
            into("META-INF")
        }
    }
    val fatJar = register<Jar>("fatJar") {
        group = "build"
        dependsOn.addAll(
            listOf(
                "compileJava",
                "compileKotlin",
                "processResources"
            )
        )
        archiveClassifier.set("fat")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes(mapOf("Main-Class" to application.mainClass))
        }
        val contents =
            configurations
                .runtimeClasspath
                .get()
                .map { if (it.isDirectory) it else zipTree(it) }
                .plus(sourceSets.main.get().output)
        from(contents)
    }
    build {
        dependsOn(fatJar)
    }
    test {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(javaVersion.toInt())
}

application {
    mainClass.set("MainKt")
}
