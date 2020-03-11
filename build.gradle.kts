@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform") version "1.3.70"
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))

                implementation("de.westermann:KObserve-metadata:0.9.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))

                implementation("de.westermann:KObserve-jvm:0.9.3")
                implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")

                implementation("org.openjfx:javafx-controls:13:win")
                implementation("org.openjfx:javafx-graphics:13:win")
                implementation("org.openjfx:javafx-base:13:win")

                implementation("org.openjfx:javafx-controls:13:linux")
                implementation("org.openjfx:javafx-graphics:13:linux")
                implementation("org.openjfx:javafx-base:13:linux")

                implementation("org.openjfx:javafx-controls:13:mac")
                implementation("org.openjfx:javafx-graphics:13:mac")
                implementation("org.openjfx:javafx-base:13:mac")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
    }
}

val mainClassName = "de.robolab.jfx.Launcher"

val jvmJar = tasks.named<Jar>("jvmJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes("Main-Class" to mainClassName)
    }

    from(Callable { configurations["jvmRuntimeClasspath"].map { if (it.isDirectory) it else zipTree(it) } })
}

tasks.create<JavaExec>("run") {
    dependsOn("jvmJar")

    group = "application"
    main = mainClassName
    classpath(jvmJar)
    args()
}
