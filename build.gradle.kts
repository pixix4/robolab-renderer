@file:Suppress("UNUSED_VARIABLE", "SuspiciousCollectionReassignment", "PropertyName")

import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

val FRONTEND_VERSION = "4.0.1"
val BACKEND_VERSION = "1.0.0"

plugins {
    kotlin("multiplatform") version "1.5.21"
    kotlin("plugin.serialization") version "1.5.21"
    id("com.gorylenko.gradle-git-properties") version "2.2.4"
}

repositories {
    mavenCentral()
}

val serializationVersion = "1.2.1"
val datetimeVersion = "0.2.0"
val coroutineVersion = "1.5.0"
val ktorVersion = "1.6.0"

@Suppress("LeakingThis")
open class NodeExec : AbstractExecTask<NodeExec>(NodeExec::class.java) {

    private val nodeJs = org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.apply(project.rootProject)

    private val e by lazy {
        nodeJs.requireConfigured().nodeExecutable
    }

    override fun exec() {
        executable = e
        super.exec()
    }

    init {
        dependsOn(nodeJs.npmInstallTaskProvider, "kotlinNpmInstall", "kotlinNodeJsSetup")
    }
}

@Suppress("LeakingThis")
open class YarnExec : AbstractExecTask<YarnExec>(YarnExec::class.java) {

    private val nodeJs = org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin.apply(project.rootProject)

    private val e by lazy {
        nodeJs.requireConfigured().nodeExecutable
    }

    private val yarnPath by lazy {
        project.yarn.installationDir
            .resolve("yarn-v${project.yarn.version}")
            .resolve("bin/yarn.js")
            .absolutePath
    }

    override fun exec() {
        executable = e

        val oldArgs = args?.toList() ?: emptyList()
        if (oldArgs.firstOrNull() != yarnPath) {
            setArgs(listOf(yarnPath) + oldArgs)
        }

        println("$executable ${args?.joinToString(" ")}")

        super.exec()
    }

    init {
        dependsOn(nodeJs.npmInstallTaskProvider, "kotlinNpmInstall", "kotlinNodeJsSetup", "kotlinYarnSetup")
    }
}

kotlin {
    js("jsFrontendCommon", IR) {
        browser()
    }
    js("jsFrontendWeb", IR) {
        browser {
            binaries.executable()
        }
        compilations.all {
            kotlinOptions {
                moduleKind = "commonjs"
            }
        }
    }
    js("jsFrontendElectron", IR) {
        browser {
            binaries.executable()
        }
        compilations.all {
            kotlinOptions {
                moduleKind = "commonjs"
            }
        }
    }


    js("jsBackendCommon", IR) {
        nodejs()
    }
    js("jsBackend", IR) {
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
            languageSettings.useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
            languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
        }

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion")

                implementation("dev.gitlive:kotlin-diff-utils:5.0.7")

                api("io.ktor:ktor-client-core:$ktorVersion")
            }
        }

        val jsFrontendCommonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-js:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutineVersion")
                implementation("io.ktor:ktor-client-js:$ktorVersion")

                implementation(npm("mqtt", "4.2.8"))
                implementation(npm("abort-controller", "3.0.0"))
                implementation(npm("text-encoding", "0.7.0"))
                implementation(npm("hammerjs", "2.0.8"))
                implementation(npm("moment", "2.29.1"))
                implementation(npm("url", "0.11.0"))

                implementation(devNpm("sass", "1.32.2"))
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

                implementation(npm("mqtt", "4.2.8"))
                implementation(npm("abort-controller", "3.0.0"))
                implementation(npm("text-encoding", "0.7.0"))
                implementation(npm("hammerjs", "2.0.8"))
                implementation(npm("moment", "2.29.1"))
                implementation(npm("@js-joda/core", "3.2.0"))
                implementation(npm("buffer", "6.0.3"))
                implementation(npm("process", "0.11.10"))
                implementation(npm("url", "0.11.0"))

                implementation(devNpm("sass", "1.32.2"))
            }
        }

        val jsFrontendElectronMain by getting {
            dependencies {
                dependsOn(jsFrontendCommonMain)

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-js:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutineVersion")
                implementation("io.ktor:ktor-client-js:$ktorVersion")

                implementation(npm("mqtt", "4.2.8"))
                implementation(npm("abort-controller", "3.0.0"))
                implementation(npm("text-encoding", "0.7.0"))
                implementation(npm("hammerjs", "2.0.8"))
                implementation(npm("moment", "2.29.1"))

                implementation(devNpm("sass", "1.32.2"))
            }
        }

        val jsBackendMain by getting {
            dependencies {
                dependsOn(jsBackendCommonMain)

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-js:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutineVersion")
                implementation("io.ktor:ktor-client-js:$ktorVersion")

                implementation(npm("ioredis", "4.19.4"))
                implementation(npm("express", "4.17.1"))
                implementation(npm("socket.io", "3.0.5"))
                implementation(npm("text-encoding", "0.7.0"))
                implementation(npm("body-parser", "1.19.0"))
                implementation(npm("jsonwebtoken", "8.5.1"))
                implementation(npm("cookie-parser", "1.4.5"))
                implementation(npm("pg", "8.5.1"))
                implementation(npm("canvas", "2.8.0"))
                implementation(npm("express-fileupload", "1.2.0"))
                implementation(npm("moment", "2.29.1"))
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
            tags = ${git["git.tags"]}
            lastTag = ${git["git.closest.tag"] ?: ""}
            lastTagDiff = ${git["git.closest.tag.commit.count"]}
            dirty = ${git["git.dirty"]}
            commitCount = ${git["git.total.commit.count"]}
        """.trimIndent()
        )
    }

    outputs.upToDateWhen { false }
    outputs.files(webpackFile, iniFile)
}

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
tasks.create<NodeExec>("jsFrontendWebCompileSass") {
    dependsOn("jsFrontendWebProcessResources")

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
tasks.create<NodeExec>("jsFrontendElectronCompileSass") {
    dependsOn("jsFrontendElectronProcessResources")

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

tasks.named("jsFrontendWebBrowserProductionExecutableDistributeResources") {
    dependsOn("jsFrontendWebCompileSass")
}

tasks.named("jsFrontendElectronBrowserProductionExecutableDistributeResources") {
    dependsOn("jsFrontendElectronCompileSass")
}

tasks.named("jsFrontendWebProductionExecutableCompileSync") {
    dependsOn("jsFrontendWebCompileSass")
}

tasks.named("jsFrontendElectronProductionExecutableCompileSync") {
    dependsOn("jsFrontendElectronCompileSass")
}

tasks.named("jsFrontendWebDevelopmentExecutableCompileSync") {
    dependsOn("jsFrontendWebCompileSass")
}

tasks.named("jsFrontendElectronDevelopmentExecutableCompileSync") {
    dependsOn("jsFrontendElectronCompileSass")
}

tasks.named("jsFrontendWebJar") {
    dependsOn("jsFrontendWebCompileSass")
}

tasks.named("jsFrontendElectronJar") {
    dependsOn("jsFrontendElectronCompileSass")
}

tasks.create("jsFrontendElectronNpmVersion") {
    doLast {
        val file = File("${projectDir}/electron/package.json")
        val lines = file.readLines()
        val newLines = lines.map { line ->
            if ("\"version\":" in line) {
                "  \"version\": \"$FRONTEND_VERSION\","
            } else line
        }
        file.writeText(newLines.joinToString("\n"))
    }
}


tasks.create<YarnExec>("jsFrontendElectronNpmInstall") {
    dependsOn("jsFrontendElectronNpmVersion")

    workingDir = file("electron")
    args("install")
}


tasks.create<Delete>("cleanJsFrontendSync") {
    delete(
        "${projectDir}/deploy/distWeb",
        "${projectDir}/deploy/distElectron",
        "${projectDir}/webpack.config.d/build.js",
        "${projectDir}/webpack.config.d/target.js",
        "${projectDir}/electron/dist",
        "${projectDir}/electron/node_modules"
    )
}

tasks.create<Delete>("cleanJsBackendSync") {
    delete("${projectDir}/deploy/distServer")
}

tasks.named("clean") {
    dependsOn("cleanJsFrontendSync", "cleanJsBackendSync")
}

val jsFrontendWebTargetFile = tasks.create("jsFrontendWebTargetFile") {
    dependsOn("generateGitProperties")
    val file = File("$projectDir/webpack.config.d/target.js")

    doLast {
        file.writeText(
            """
            config.target = "web"
            
            config.externals = [
                (function () {
                    var IGNORES = [
                        'electron'
                    ];
                    return function ({context, request}, callback) {
                        if (IGNORES.indexOf(request) >= 0) {
                            return callback(null, "require('" + request + "')");
                        }
                        return callback();
                    };
                })()
            ]
            """.trimIndent()
        )
    }

    outputs.files(file)
}

val jsFrontendElectronTargetFile = tasks.create("jsFrontendElectronTargetFile") {
    dependsOn("generateGitProperties")
    val file = File("$projectDir/webpack.config.d/target.js")

    doLast {
        file.writeText(
            """
            config.target = "electron-renderer"
            
            config.externals = {
                "fs": "fs"
            }
            """.trimIndent()
        )
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

val deployWeb = tasks.create<Sync>("deployWeb") {
    dependsOn("jsFrontendWebBrowserProductionWebpack", "jsFrontendWebJar")

    val file =
        tasks.named("jsFrontendWebBrowserProductionWebpack")
            .get().outputs.files.files.first { it.name == "${project.name}.js" }
    val sourceMap = file.resolveSibling("${project.name}.js.map")
    from(
        file,
        sourceMap,
        Callable { zipTree(tasks.get("jsFrontendWebJar").outputs.files.first()) }
    )

    exclude(
        "${project.name}-js-frontend-web/**",
        "${project.name}-js-frontend-web.js",
        "${project.name}-js-frontend-web.js.map",
        "${project.name}-js-frontend-web.meta.js",
        "package.json",
        "META-INF/**",
        "**/*.scss",
        "default"
    )

    into("${projectDir}/deploy/distWeb/")
}
val deployBackend = tasks.create<Sync>("deployBackend") {
    dependsOn(
        "compileKotlinJsBackend",
        "jsBackendProductionExecutableCompileSync",
        "jsBackendPackageJson",
        "kotlinNpmInstall",
        "jsBackendProcessResources",
        generateBuildInformation
    )

    from("$buildDir/js") {
        exclude {
            it.name.contains("js-frontend") || it.name.contains("-test") || it.name.contains(".bin") || Files.isSymbolicLink(
                Paths.get(it.path)
            )
        }

    }
    from(generateBuildInformation)
    from("$buildDir/processedResources/jsBackend/main")

    into("${projectDir}/deploy/distServer")
}

tasks.create<NodeExec>("runBackend") {
    dependsOn(deployBackend)

    workingDir = file("deploy/distServer")

    args(
        "packages/${project.name}-js-backend/kotlin/${project.name}-js-backend.js",
        "${projectDir}/server.ini"
    )
}

tasks.create<NodeExec>("runWeb") {
    dependsOn(deployBackend, deployWeb)

    workingDir = file("deploy/distServer")

    args(
        "packages/${project.name}-js-backend/kotlin/${project.name}-js-backend.js",
        "${projectDir}/server.ini"
    )
}

val deployElectron = tasks.create<Sync>("deployElectron") {
    dependsOn("jsFrontendElectronBrowserProductionWebpack", "jsFrontendElectronJar", "jsFrontendElectronNpmVersion")

    val file = tasks.named("jsFrontendElectronBrowserProductionWebpack")
        .get().outputs.files.files.first { it.name == "${project.name}.js" }
    val sourceMap = file.resolveSibling("${project.name}.js.map")
    from(
        file,
        sourceMap,
        Callable { zipTree(tasks.get("jsFrontendElectronJar").outputs.files.first()) }
    )

    exclude(
        "${project.name}-js-frontend-electron/**",
        "${project.name}-js-frontend-electron.js",
        "${project.name}-js-frontend-electron.js.map",
        "${project.name}-js-frontend-electron.meta.js",
        "package.json",
        "META-INF/**",
        "*.scss"
    )

    into("${projectDir}/deploy/distElectron/")
}

tasks.create<YarnExec>("runElectron") {
    dependsOn(deployElectron, "jsFrontendElectronNpmInstall")

    workingDir = file("electron")

    args(
        "dev"
    )
}

tasks.create<YarnExec>("packElectron") {
    dependsOn(deployElectron, "jsFrontendElectronNpmInstall")

    workingDir = file("electron")

    args(
        "pack"
    )
}
