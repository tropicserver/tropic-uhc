import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    kotlin("jvm") version "1.8.20"
    kotlin("kapt") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

allprojects {
    group = "gg.tropic.uhc"
    version = "1.0.0"

    repositories {
        mavenCentral()
        configureScalaRepository()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "maven-publish")

    dependencies {
        compileOnly(kotlin("stdlib"))
    }

    kotlin {
        jvmToolchain(jdkVersion = 17)
    }

    tasks.withType<ShadowJar> {
        archiveClassifier.set("")
        exclude(
            "**/*.kotlin_metadata",
            "**/*.kotlin_builtins",
            "META-INF/"
        )

        archiveFileName.set(
            "uhc-${project.name}.jar"
        )
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.javaParameters = true
        kotlinOptions.jvmTarget = "17"
    }

    publishing {
        repositories.configureScalaRepository()

        publications {
            register(
                name = "mavenJava",
                MavenPublication::class
            ) {
                from(components["java"])
                artifact(tasks["shadowJar"])
            }
        }
    }

    tasks.getByName("build")
        .dependsOn(
            "shadowJar",
            "publishShadowPublicationToScalaRepository"
        )
}

fun RepositoryHandler.configureScalaRepository()
{
    maven("${property("artifactory_contextUrl")}/gradle-release") {
        credentials {
            username = property("artifactory_user").toString()
            password = property("artifactory_password").toString()
        }
    }
}
