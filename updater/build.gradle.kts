import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.0")
    implementation("com.squareup.okhttp3:okhttp:4.8.1")
    implementation("com.github.ajalt:clikt:2.8.0")

    implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")

    implementation("org.openjfx:javafx-controls:14:win")
    implementation("org.openjfx:javafx-graphics:14:win")
    implementation("org.openjfx:javafx-base:14:win")

    implementation("org.openjfx:javafx-controls:14:linux")
    implementation("org.openjfx:javafx-graphics:14:linux")
    implementation("org.openjfx:javafx-base:14:linux")

    implementation("org.openjfx:javafx-controls:14:mac")
    implementation("org.openjfx:javafx-graphics:14:mac")
    implementation("org.openjfx:javafx-base:14:mac")

    testImplementation(kotlin("test-junit"))
}
tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-Xuse-experimental=kotlin.contracts.ExperimentalContracts"
        freeCompilerArgs += "-Xinline-classes"
    }
}

val mainClassName = "de.robolab.updater.Launcher"

val jar = tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes("Main-Class" to mainClassName)
    }

    from(Callable {
        configurations["runtimeClasspath"].map {
            if (it.isDirectory) it else zipTree(it)
        }
    }) {
        exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*SF")
    }
}

tasks.create<JavaExec>("run") {
    dependsOn("jar")

    group = "application"
    main = mainClassName
    classpath(jar)
    args()
}
