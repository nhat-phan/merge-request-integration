import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val artifactGroup: String by project
val artifactVersion: String by project
val intellijVersion: String by project
val jvmTarget: String by project
val foundationVersion: String by project
val gitlab4jVersion: String by project
val prettyTimeVersion: String by project
val commonmarkVersion: String by project

group = artifactGroup
version = artifactVersion

repositories {
    jcenter()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.github.nhat-phan.foundation:foundation-jvm:$foundationVersion")
    implementation("org.gitlab4j:gitlab4j-api:$gitlab4jVersion")
    implementation("org.ocpsoft.prettytime:prettytime:$prettyTimeVersion")
    compile("com.atlassian.commonmark:commonmark:$commonmarkVersion")

    implementation(project(":contracts"))
    implementation(project(":intellij-idea-plugin-core"))
    implementation(project(":merge-request-integration"))
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = intellijVersion
    updateSinceUntilBuild = false
    setPlugins("git4idea")
}

val compileKotlin: KotlinCompile by tasks
val compileTestKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = jvmTarget
}

compileTestKotlin.kotlinOptions {
    jvmTarget = jvmTarget
}

tasks {
    named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
        kotlinOptions {
            jvmTarget = jvmTarget
        }
    }
    
    named<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
        changeNotes(htmlFixer("./intellij-idea-plugin-ee/doc/release-notes.$artifactVersion.html"))
        pluginDescription(htmlFixer("./intellij-idea-plugin-ee/doc/description.html"))
    }
}

fun htmlFixer(filename: String): String {
    if (!File(filename).exists()) {
        throw Exception("File $filename not found.")
    }
    return File(filename).readText().replace("<html lang=\"en\">", "").replace("</html>", "")
}