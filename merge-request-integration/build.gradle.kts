val artifactGroup: String by project
val artifactVersion: String by project
val jvmTarget: String by project
val foundationVersion: String by project
val foundationProcessorVersion: String by project
val kotlinxSerializationRuntimeVersion: String by project
val javaFakerVersion: String by project
val jodaTimeVersion: String by project
val fuelVersion: String by project
val gitlab4jVersion: String by project
val prettyTimeVersion: String by project
val commonmarkVersion: String by project

group = artifactGroup
version = artifactVersion

repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.nhat-phan.foundation:foundation-jvm:$foundationVersion")
    implementation(project(":contracts"))
    implementation("joda-time:joda-time:$jodaTimeVersion")
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("org.gitlab4j:gitlab4j-api:$gitlab4jVersion")
    implementation("org.ocpsoft.prettytime:prettytime:$prettyTimeVersion")

    compile("com.atlassian.commonmark:commonmark:$commonmarkVersion")
    compile("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationRuntimeVersion")
    compile("com.github.javafaker:javafaker:$javaFakerVersion")

    kapt("com.github.nhat-phan.foundation:foundation-processor:$foundationProcessorVersion")
    kaptTest("com.github.nhat-phan.foundation:foundation-processor:$foundationProcessorVersion")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

kapt {
    arguments {
        arg("foundation.processor.globalNamespace", "net.ntworld.mergeRequestIntegration")
    }
}

tasks {
    named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
        kotlinOptions {
            jvmTarget = jvmTarget
        }
    }
}
