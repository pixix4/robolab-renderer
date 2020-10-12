@file:Suppress("UNUSED_VARIABLE", "SuspiciousCollectionReassignment")

import java.text.SimpleDateFormat
import java.util.*

plugins {
    kotlin("multiplatform") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.gorylenko.gradle-git-properties") version "2.2.2"
}

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        url = uri("https://dl.bintray.com/kotlin/ktor")
    }
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlinx")
    }
    maven {
        url  = uri("https://kotlin.bintray.com/kotlinx")
    }
}

val serializationVersion = "1.0.0"
val klockVersion = "1.12.0"
val coroutineVersion = "1.3.9"
val ktorVersion = "1.4.1"
kotlin {
    jvm("jvm") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs += "-Xuse-experimental=kotlin.contracts.ExperimentalContracts"
                freeCompilerArgs += "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes"
            }
        }
    }
    js("jsClient") {
        browser {
            binaries.executable()
            @Suppress("EXPERIMENTAL_API_USAGE")
            dceTask {
                keep(
                    "ktor-ktor-io.\$\$importsForInline\$\$.ktor-ktor-io.io.ktor.utils.io"
                )
            }
        }
        compilations.all {
            kotlinOptions {
                moduleKind = "commonjs"
                freeCompilerArgs += "-Xuse-experimental=kotlin.contracts.ExperimentalContracts"
                freeCompilerArgs += "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes"
            }
        }
    }
    js("jsServer") {
        nodejs {
            binaries.executable()
        }
        compilations.all {
            kotlinOptions {
                moduleKind = "commonjs"
                freeCompilerArgs += "-Xuse-experimental=kotlin.contracts.ExperimentalContracts"
                freeCompilerArgs += "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
                implementation("com.soywiz.korlibs.klock:klock:$klockVersion")

                implementation("dev.gitlive:kotlin-diff-utils:4.1.6")

                api("io.ktor:ktor-client-core:$ktorVersion")
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
                implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.0")

                implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
                implementation("com.github.ajalt.clikt:clikt:3.0.1")

                implementation("org.openjfx:javafx-controls:14:win")
                implementation("org.openjfx:javafx-graphics:14:win")
                implementation("org.openjfx:javafx-base:14:win")

                implementation("org.openjfx:javafx-controls:14:linux")
                implementation("org.openjfx:javafx-graphics:14:linux")
                implementation("org.openjfx:javafx-base:14:linux")

                implementation("org.openjfx:javafx-controls:14:mac")
                implementation("org.openjfx:javafx-graphics:14:mac")
                implementation("org.openjfx:javafx-base:14:mac")

                implementation("org.fxmisc.flowless:flowless:0.6.1")
                implementation("org.fxmisc.richtext:richtextfx:0.10.5")
                implementation("org.fusesource.jansi:jansi:1.18")
                implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.4")
                implementation("org.slf4j:slf4j-nop:1.7.30")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:$coroutineVersion")

                implementation("com.h2database:h2:1.4.199")
                implementation("org.jetbrains.exposed:exposed-core:0.27.1")
                implementation("org.jetbrains.exposed:exposed-jdbc:0.27.1")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-apache:$ktorVersion")
                implementation("com.squareup.okhttp3:okhttp:4.8.1")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        val jsClientMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-js:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutineVersion")
                implementation("io.ktor:ktor-client-js:$ktorVersion")


                implementation(npm("mqtt", "4.1.0"))
                implementation(npm("abort-controller", "3.0.0"))
                implementation(npm("text-encoding", "0.7.0"))
                implementation(npm("hammerjs", "2.0.8"))

                implementation(devNpm("sass","1.26.10"))
            }
        }
        val jsClientTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }

        val jsServerMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-js:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutineVersion")
                implementation("io.ktor:ktor-client-js:$ktorVersion")

                implementation("com.github.ajalt.clikt:clikt:3.0.1")

                implementation(npm("ioredis", "4.17.3"))
                implementation(npm("express", "4.17.1"))
                implementation(npm("socket.io", "2.3.0"))
                implementation(npm("text-encoding", "0.7.0"))
                implementation(npm("body-parser","1.19.0"))
                implementation(npm("jsonwebtoken","8.5.1"))
            }
        }
        val jsServerTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }
}

gitProperties {
    extProperty = "gitProps"
    dateFormat = "yyyy-MM-dd HH:mm:ss z"
    dateFormatTimeZone = "UTC"
}
val generateGitProperties = tasks.named<com.gorylenko.GenerateGitPropertiesTask>("generateGitProperties") {
    outputs.upToDateWhen { false }
}

val createBuildInfo = tasks.create("createBuildInfo") {
    dependsOn("generateGitProperties")
    val file = File("$buildDir/processedResources/build.ini")

    doLast {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
        format.timeZone = TimeZone.getTimeZone("UTC")
        val buildTime = format.format(Date())

        @Suppress("UNCHECKED_CAST") val git = project.ext["gitProps"] as Map<String, String>

        val version = File("$projectDir/version.ini").readText()
        file.writeText(
            """
            [build]
            time = $buildTime
            javaVersion = ${System.getProperty("java.version")}
            javaVendor = ${System.getProperty("java.vm.name")}
            gradleVersion = gradle-${project.gradle.gradleVersion}
            systemName = ${System.getProperty("os.name")}
            systemVersion = ${System.getProperty("os.version")}
            user = ${System.getProperty("user.name")}
            
            [vcs]
            branch = ${git["git.branch"]}
            commitHash = ${git["git.commit.id.abbrev"]}
            commitMessage = ${git["git.commit.message.short"]}
            commitTime = ${git["git.commit.time"]}
            tags = ${git["git.tags"] }
            lastTag = ${git["git.closest.tag"] ?: ""}
            lastTagDiff = ${git["git.closest.tag.commit.count"]}
            dirty = ${git["git.dirty"]}
            commitCount = ${git["git.total.commit.count"]}


        """.trimIndent() + version
        )
    }

    inputs.file(file("$projectDir/version.ini"))
    outputs.file(file)
}

val mainClassName = "de.robolab.client.Launcher"

val jvmJar = tasks.named<Jar>("jvmJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes("Main-Class" to mainClassName)
    }

    from(Callable {
        configurations["jvmRuntimeClasspath"].map {
            if (it.isDirectory) it else zipTree(it)
        }
    }) {
        exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*SF")
    }
}

tasks.create<JavaExec>("jvmRun") {
    dependsOn("jvmJar")

    group = "application"
    main = mainClassName
    classpath(jvmJar)
    args()
}

tasks.create<JavaExec>("buildSassTheme") {
    dependsOn("jvmJar")

    group = "application"
    main = "de.robolab.client.utils.ThemeGenerator"
    classpath(jvmJar)
    args()
}

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
tasks.create<Exec>("jsClientCompileSass") {
    dependsOn("kotlinNpmInstall", "jsClientProcessResources")

    doFirst {
        val nodeJs =
            rootProject.extensions.getByName(org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension.Companion.EXTENSION_NAME) as org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

        if (nodeJs.installationDir.list() == null) {
            return@doFirst
        }

        val nodeJsDir =
            nodeJs.installationDir
                .resolve(nodeJs.installationDir.list().first())

        val executableFile = nodeJsDir.listFiles().find { it.nameWithoutExtension == nodeJs.nodeCommand }
            ?: nodeJsDir.resolve("bin").listFiles().find { it.nameWithoutExtension == nodeJs.nodeCommand }
            ?: nodeJsDir.resolve("bin").resolve(nodeJs.nodeCommand)

        executable(executableFile)
    }

    args(
        "$buildDir/js/node_modules/sass/sass.js",
        "$projectDir/src/jsClientMain/resources/public/stylesheets/style.scss",
        "$buildDir/processedResources/jsClient/main/public/stylesheets/style.css"
    )

    outputs.cacheIf { true }
    inputs.dir(file("$projectDir/src/jsClientMain/resources/public/stylesheets"))
        .withPropertyName("stylesheets")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    outputs.file("$buildDir/processedResources/jsClient/main/public/stylesheets/style.css")
        .withPropertyName("style")
}

tasks.named("jsClientBrowserDistributeResources") {
    dependsOn("jsClientCompileSass")
}
tasks.named("jsClientBrowserDevelopmentRun") {
    dependsOn("jsClientCompileSass")
}
tasks.named("jsClientBrowserProductionRun") {
    dependsOn("jsClientCompileSass")
}

val jsClientBrowserProductionWebpack = tasks.named("jsClientBrowserProductionWebpack")

val jsClientJar = tasks.named<Jar>("jsClientJar") {
    dependsOn("jsClientBrowserProductionWebpack", createBuildInfo)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val file = jsClientBrowserProductionWebpack.get().outputs.files.files.first { it.name == "robolab.js" }
    val sourceMap = file.resolveSibling("robolab.js.map")
    from(file, sourceMap)
    from(createBuildInfo)
}

tasks.create<Sync>("jsClientSync") {
    dependsOn("jsClientJar")

    from(Callable { zipTree(jsClientJar.get().archiveFile) })
    into("${projectDir}/web/website")
}

tasks.create<Sync>("jvmClientSync") {
    dependsOn("jvmJar")

    from(jvmJar)
    from(Callable { zipTree(jvmJar.get().archiveFile) }) {
        include("build.ini")
    }

    into("${projectDir}/web/website/jvm")
    rename {
        if (it == "robolab-jvm.jar") "robolab-renderer.jar" else it
    }
}

tasks.create<Sync>("jsServerSync") {
    dependsOn("compileKotlinJsServer", "jsServerPackageJson", "kotlinNpmInstall", createBuildInfo)

    from("$buildDir/js")
    from(createBuildInfo)
    into("${projectDir}/webServer")
}

tasks.create<Delete>("cleanJsClientSync") {
    delete("${projectDir}/web/website")
}

tasks.create<Delete>("cleanJsServerSync") {
    delete("${projectDir}/webServer")
}

tasks.named("clean") {
    dependsOn("cleanJsClientSync", "cleanJsServerSync")
}

tasks.named<ProcessResources>("jvmProcessResources") {
    dependsOn(createBuildInfo)

    from(createBuildInfo)
}
tasks.named<ProcessResources>("jsClientProcessResources") {
    dependsOn(createBuildInfo)

    from(createBuildInfo)
}
tasks.named<ProcessResources>("jsServerProcessResources") {
    dependsOn(createBuildInfo)

    from(createBuildInfo)
}
