@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform") version "1.3.70"
    id("com.github.node-gradle.node") version "2.2.3"
    id("org.kravemir.gradle.sass") version "1.2.4"
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
    js()

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
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))

                implementation("de.westermann:KObserve-js:0.9.3")
            }
        }
        val jsTest by getting {
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

tasks.create<JavaExec>("jvmRun") {
    dependsOn("jvmJar")

    group = "application"
    main = mainClassName
    classpath(jvmJar)
    args()
}

sass {
    create("main") {
        srcDir = file("$projectDir/src/jsMain/resources/public/stylesheets")
        outDir = file("$buildDir/processedResources/js/main/public/stylesheets/")
    }
}

val jsJar = tasks.named<Jar>("jsJar") {
    dependsOn("mainSass")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(Callable { configurations["jsRuntimeClasspath"].map { if (it.isDirectory) it else zipTree(it) } })
}

val jsSync = tasks.create<Sync>("jsSync") {
    dependsOn("jsJar")

    from(Callable { zipTree(jsJar.get().archiveFile) })
    into("${projectDir}/web/website")
}

tasks.create<com.moowork.gradle.node.task.NodeTask>("jsRun") {
    dependsOn("jsSync", "npmInstall")
    setScript(file("web/index.js"))
}

node {
    download = true
    workDir = file("${project.projectDir}/build/node")
    npmWorkDir = file("${project.projectDir}/web")
    nodeModulesDir = file("${project.projectDir}/web")
}
