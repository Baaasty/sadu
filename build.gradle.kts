plugins {
    java
    `maven-publish`
    `java-library`
    id("de.chojo.publishdata") version "1.0.8"
    id("org.cadixdev.licenser") version "0.6.1"
}

group = "de.chojo.sadu"
version = "1.0.0"

dependencies {
    api(project(":sadu-sqlite"))
    api(project(":sadu-postgresql"))
    api(project(":sadu-mariadb"))
    api(project(":sadu-mysql"))
    api(project(":sadu-updater"))
    api(project(":sadu-datasource"))
}

subprojects {
    apply {
        // We want to apply several plugins to subprojects
        plugin<JavaPlugin>()
        plugin<org.cadixdev.gradle.licenser.Licenser>()
        plugin<de.chojo.PublishData>()
        plugin<JavaLibraryPlugin>()
        plugin<MavenPublishPlugin>()
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://eldonexus.de/repository/maven-public/")
        maven("https://eldonexus.de/repository/maven-proxies/")
    }

    java {
        withSourcesJar()
        withJavadocJar()
        sourceCompatibility = JavaVersion.VERSION_15
    }

    dependencies {
        testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.8.1")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    }

    license {
        header(rootProject.file("HEADER.txt"))
        include("**/*.java")
    }

    publishData {
        useEldoNexusRepos(true)
        publishComponent("java")
    }

    publishing {
        publications.create<MavenPublication>("maven") {
            publishData.configurePublication(this)
            pom {
                url.set("https://github.com/rainbowdashlabs/sadu")
                developers {
                    developer {
                        name.set("Florian Fülling")
                        url.set("https://github.com/rainbowdashlabs")
                    }
                }
                licenses {
                    license {
                        name.set("GNU Affero General Public License v3.0")
                        url.set("https://github.com/rainbowdashlabs/sadu/blob/main/LICENSE.md")
                    }
                }
            }
        }

        repositories {
            maven {
                authentication {
                    credentials(PasswordCredentials::class) {
                        username = System.getenv("NEXUS_USERNAME")
                        password = System.getenv("NEXUS_PASSWORD")
                    }
                }

                setUrl(publishData.getRepository())
                name = "EldoNexus"
            }
        }
    }

    // We configure some general tasks for our modules
    tasks {
        test {
            dependsOn(licenseCheck)
            useJUnitPlatform()
            testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }
}

tasks {
    register<Javadoc>("alljavadoc") {
        setDestinationDir(file("${buildDir}/docs/javadoc"))
        val projects = project.rootProject.allprojects.filter { p -> !p.name.contains("example") }
        setSource(projects.map { p -> p.sourceSets.main.get().allJava })
        classpath = files(projects.map { p -> p.sourceSets.main.get().compileClasspath })
    }
}
