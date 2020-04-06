@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform") version "1.3.71"
    id("com.github.node-gradle.node") version "2.2.3"
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

                // implementation("de.westermann:KObserve-metadata:0.9.3")
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

                // implementation("de.westermann:KObserve-jvm:0.9.3")
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

                // implementation("de.westermann:KObserve-js:0.9.3")
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

val jsInstallSass = tasks.create<com.moowork.gradle.node.npm.NpmTask>("jsInstallSass") {
    setArgs(listOf("install", "sass"))

    outputs.cacheIf { true }
    outputs.dir(file("${project.projectDir}/web/node_modules/sass"))
            .withPropertyName("sassCompiler")

}

val jsCompileSass = tasks.create<Exec>("jsCompileSass") {
    dependsOn("jsInstallSass", "jsProcessResources")

    commandLine(
            "$projectDir/web/node_modules/sass/sass.js",
            "$projectDir/src/jsMain/resources/public/stylesheets/style.scss",
            "$buildDir/processedResources/js/main/public/stylesheets/style.css"
    )

    outputs.cacheIf { true }
    inputs.dir(file("$projectDir/src/jsMain/resources/public/stylesheets"))
            .withPropertyName("stylesheets")
            .withPathSensitivity(PathSensitivity.RELATIVE)

    outputs.file("$buildDir/processedResources/js/main/public/stylesheets/style.css")
            .withPropertyName("style")
}

val jsJar = tasks.named<Jar>("jsJar") {
    dependsOn("jsCompileSass")
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
    script = file("web/index.js")
}

node {
    download = true
    workDir = file("${project.projectDir}/build/node")
    npmWorkDir = file("${project.projectDir}/web")
    nodeModulesDir = file("${project.projectDir}/web")
}
