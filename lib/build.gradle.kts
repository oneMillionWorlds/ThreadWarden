/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java library project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.6/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the java-library plugin for API and implementation separation.
    id("java-library")
    id("java-gradle-plugin")
    id("maven-publish")
    id("signing")
}

group = "com.onemillionworlds"
version = "1.0.0"

repositories {
    mavenCentral()
}

java{
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.jmonkey.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly(libs.jmonkey.core)
    implementation(libs.byte.buddy)
    implementation(libs.byte.buddyagent)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = "thread-warden"
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set(project.name)
                description.set("A library to detect incorrect access of the JME scene graph from the wrong thread")
                url.set("https://github.com/oneMillionWorlds/ThreadWarden")
                licenses {
                    license {
                        name.set("New BSD (3-clause) License")
                        url.set("http://opensource.org/licenses/BSD-3-Clause")
                    }
                }
                scm {
                    connection.set("git@github.com:oneMillionWorlds/ThreadWarden.git")
                    developerConnection.set("git@github.com:oneMillionWorlds/ThreadWarden.git")
                    url.set("https://github.com/oneMillionWorlds/ThreadWarden")
                }
                developers {
                    developer {
                        id.set("RichardTingle")
                        name.set("Richard Tingle (aka richtea)")
                        email.set("support@oneMillionWorlds.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            // url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            credentials {
                username = findProperty("ossrhUsername")?.toString() ?: ""
                password = findProperty("ossrhPassword")?.toString() ?: ""
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
