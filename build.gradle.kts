@file:Suppress("UNUSED_VARIABLE", "SuspiciousCollectionReassignment", "PropertyName")

import java.text.SimpleDateFormat
import java.util.*

val FRONTEND_VERSION = "4.0.0"
val BACKEND_VERSION = "1.0.0"

plugins {
    kotlin("multiplatform") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
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

val serializationVersion = "1.0.1"
val klockVersion = "1.12.0"
val coroutineVersion = "1.4.2"
val ktorVersion = "1.4.3"

kotlin {
    js("jsFrontendCommon") {
        browser()
    }
    js("jsFrontendWeb") {
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
            }
        }
    }
    js("jsFrontendElectron") {
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
            }
        }
    }


    js("jsBackendCommon") {
        browser()
        nodejs()
    }
    js("jsBackend") {
        nodejs {
            binaries.executable()
        }
        compilations.all {
            kotlinOptions {
                moduleKind = "commonjs"
            }
        }
    }

    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
            languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
        }

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
                implementation("com.soywiz.korlibs.klock:klock:$klockVersion")

                implementation("dev.gitlive:kotlin-diff-utils:4.1.6")

                api("io.ktor:ktor-client-core:$ktorVersion")
            }
        }

        val jsFrontendCommonMain by getting {
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
        val jsBackendCommonMain by getting {
            dependencies {

            }
        }

        val jsFrontendWebMain by getting {
            dependencies {
                dependsOn(jsFrontendCommonMain)

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

        val jsFrontendElectronMain by getting {
            dependencies {
                dependsOn(jsFrontendCommonMain)
                dependsOn(jsBackendCommonMain)

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-js:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutineVersion")
                implementation("io.ktor:ktor-client-js:$ktorVersion")

                implementation("com.github.ajalt.clikt:clikt:3.0.1")

                implementation(npm("mqtt", "4.1.0"))
                implementation(npm("abort-controller", "3.0.0"))
                implementation(npm("text-encoding", "0.7.0"))
                implementation(npm("hammerjs", "2.0.8"))

                implementation(devNpm("sass","1.26.10"))
            }
        }

        val jsBackendMain by getting {
            dependencies {
                dependsOn(jsBackendCommonMain)

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
                implementation(npm("cookie-parser","1.4.5"))
                implementation(npm("pg","8.5.0"))
            }
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

val generateBuildInformation = tasks.create("generateBuildInformation") {
    dependsOn("generateGitProperties")
    val webpackFile = File("$projectDir/webpack.config.d/build.js")
    val iniFile = File("$buildDir/processedResources/build.ini")

    doLast {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
        format.timeZone = TimeZone.getTimeZone("UTC")
        val buildTime = format.format(Date())

        @Suppress("UNCHECKED_CAST") val git = project.ext["gitProps"] as Map<String, String>

        webpackFile.writeText(
            """
            const webpack = require("webpack")

            const definePlugin = new webpack.DefinePlugin(
               {
                  VERSION_FRONTEND: "\"$FRONTEND_VERSION\"",
                  VERSION_BACKEND: "\"$BACKEND_VERSION\"",
                  BUILD_TIME: "\"$buildTime\"",
                  BUILD_JAVA_VERSION: "\"${System.getProperty("java.version")}\"",
                  BUILD_JAVA_VENDOR: "\"${System.getProperty("java.vm.name")}\"",
                  BUILD_GRADLE_VERSION: "\"gradle-${project.gradle.gradleVersion}\"",
                  BUILD_SYSTEM_NAME: "\"${System.getProperty("os.name")}\"",
                  BUILD_SYSTEM_VERSION: "\"${System.getProperty("os.version")}\"",
                  BUILD_USER: "\"${System.getProperty("user.name")}\"",
                  VCS_BRANCH: "\"${git["git.branch"]}\"",
                  VCS_COMMIT_HASH: "\"${git["git.commit.id.abbrev"]}\"",
                  VCS_COMMIT_MESSAGE: "\"${git["git.commit.message.short"]}\"",
                  VCS_COMMIT_TIME: "\"${git["git.commit.time"]}\"",
                  VCS_TAGS: "\"${git["git.tags"]}\"",
                  VCS_LAST_TAG: "\"${git["git.closest.tag"] ?: ""}\"",
                  VCS_LAST_TAG_DIFF: "\"${git["git.closest.tag.commit.count"]}\"",
                  VCS_DIRTY: "\"${git["git.dirty"]}\"",
                  VCS_COMMIT_COUNT: "\"${git["git.total.commit.count"]}\""
               }
            )

            config.plugins.push(definePlugin)
             
        """.trimIndent()
        )

        iniFile.writeText(
            """
            [version]
            frontend = $FRONTEND_VERSION
            backend = $BACKEND_VERSION
                
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
        """.trimIndent()
        )
    }

    outputs.files(webpackFile, iniFile)
}

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
tasks.create<Exec>("jsFrontendWebCompileSass") {
    dependsOn("kotlinNpmInstall", "jsFrontendWebProcessResources")

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
        "$projectDir/src/jsFrontendCommonMain/resources/public/stylesheets/style.scss",
        "$buildDir/processedResources/jsFrontendWeb/main/public/stylesheets/style.css"
    )

    outputs.cacheIf { true }
    inputs.dir(file("$projectDir/src/jsFrontendCommonMain/resources/public/stylesheets"))
        .withPropertyName("stylesheets")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    outputs.file("$buildDir/processedResources/jsFrontendWeb/main/public/stylesheets/style.css")
        .withPropertyName("style")
}

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
tasks.create<Exec>("jsFrontendElectronCompileSass") {
    dependsOn("kotlinNpmInstall", "jsFrontendElectronProcessResources")

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
        "$projectDir/src/jsFrontendCommonMain/resources/public/stylesheets/style.scss",
        "$buildDir/processedResources/jsFrontendElectron/main/public/stylesheets/style.css"
    )

    outputs.cacheIf { true }
    inputs.dir(file("$projectDir/src/jsFrontendCommonMain/resources/public/stylesheets"))
        .withPropertyName("stylesheets")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    outputs.file("$buildDir/processedResources/jsFrontendElectron/main/public/stylesheets/style.css")
        .withPropertyName("style")
}

tasks.named("jsFrontendWebBrowserDistributeResources") {
    dependsOn("jsFrontendWebCompileSass")
}

tasks.create<Sync>("jsFrontendWebSync") {
    dependsOn("jsFrontendWebBrowserProductionWebpack", "jsFrontendWebJar")

    val file = tasks.named("jsFrontendWebBrowserProductionWebpack").get().outputs.files.files.first { it.name == "robolab.js" }
    val sourceMap = file.resolveSibling("robolab.js.map")
    from(
        file,
        sourceMap,
        Callable { zipTree(tasks.get("jsFrontendWebJar").outputs.files.first()) }
    )

    into("${projectDir}/deploy/distWeb/")
}

tasks.create<Sync>("jsFrontendWebSyncDev") {
    dependsOn("jsFrontendWebBrowserDevelopmentWebpack", "jsFrontendWebJar")

    val file = tasks.named("jsFrontendWebBrowserDevelopmentWebpack").get().outputs.files.files.first { it.name == "robolab.js" }
    val sourceMap = file.resolveSibling("robolab.js.map")
    from(
        file,
        sourceMap,
        Callable { zipTree(tasks.get("jsFrontendWebJar").outputs.files.first()) }
    )

    into("${projectDir}/deploy/distWeb/")
}


tasks.named("jsFrontendElectronBrowserDistributeResources") {
    dependsOn("jsFrontendElectronCompileSass")
}

tasks.create<Sync>("jsFrontendElectronSync") {
    dependsOn("jsFrontendElectronBrowserProductionWebpack", "jsFrontendElectronJar")

    val file = tasks.named("jsFrontendElectronBrowserProductionWebpack").get().outputs.files.files.first { it.name == "robolab.js" }
    val sourceMap = file.resolveSibling("robolab.js.map")
    from(
        file,
        sourceMap,
        Callable { zipTree(tasks.get("jsFrontendElectronJar").outputs.files.first()) }
    )

    into("${projectDir}/deploy/distElectron/")
}

tasks.create<Sync>("jsFrontendElectronSyncDev") {
    dependsOn("jsFrontendElectronBrowserDevelopmentWebpack", "jsFrontendElectronJar")

    val file = tasks.named("jsFrontendElectronBrowserDevelopmentWebpack").get().outputs.files.files.first { it.name == "robolab.js" }
    val sourceMap = file.resolveSibling("robolab.js.map")
    from(
        file,
        sourceMap,
        Callable { zipTree(tasks.get("jsFrontendElectronJar").outputs.files.first()) }
    )

    into("${projectDir}/deploy/distElectron/")
}

tasks.create<Sync>("jsBackendSync") {
    dependsOn("compileKotlinJsBackend", "jsBackendPackageJson", "kotlinNpmInstall", generateBuildInformation)

    from("$buildDir/js")
    from(generateBuildInformation)
    into("${projectDir}/deploy/server")
}

tasks.create<Delete>("cleanJsFrontendSync") {
    delete(
        "${projectDir}/deploy/distWeb",
        "${projectDir}/deploy/distElectron"
    )
}

tasks.create<Delete>("cleanJsBackendSync") {
    delete("${projectDir}/deploy/server")
}

tasks.named("clean") {
    dependsOn("cleanJsFrontendSync", "cleanJsBackendSync")
}

val jsFrontendWebTargetFile = tasks.create("jsFrontendWebTargetFile") {
    dependsOn("generateGitProperties")
    val file = File("$projectDir/webpack.config.d/target.js")

    doLast {
        file.writeText("""config.target = "web"""")
    }

    outputs.files(file)
}

val jsFrontendElectronTargetFile = tasks.create("jsFrontendElectronTargetFile") {
    dependsOn("generateGitProperties")
    val file = File("$projectDir/webpack.config.d/target.js")

    doLast {
        file.writeText("""config.target = "electron-renderer"""")
    }

    outputs.files(file)
}

tasks.named("jsFrontendWebBrowserDevelopmentWebpack") {
    dependsOn(generateBuildInformation, jsFrontendWebTargetFile)
}

tasks.named("jsFrontendWebBrowserProductionWebpack") {
    dependsOn(generateBuildInformation, jsFrontendWebTargetFile)
}

tasks.named("jsFrontendElectronBrowserDevelopmentWebpack") {
    dependsOn(generateBuildInformation, jsFrontendElectronTargetFile)
}

tasks.named("jsFrontendElectronBrowserProductionWebpack") {
    dependsOn(generateBuildInformation, jsFrontendElectronTargetFile)
}
