import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    alias(libs.plugins.shadow)
}

val shade: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    shade(libs.okhttp)
    shade(libs.okio)
}

val shadowJar = tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("shaded-okhttp")
    archiveClassifier.set("shaded")
    configurations.set(listOf(shade))
    relocate("okhttp3", "aegires.okhttp3")
    relocate("okio", "aegires.okio")
    exclude("META-INF/**")
    exclude("module-info.class")
}

tasks.named("jar") {
    enabled = false
}

configurations.matching { it.name == "shadowRuntimeElements" }.configureEach {
    isCanBeConsumed = false
}

listOf("apiElements", "runtimeElements").forEach { cfgName ->
    configurations.named(cfgName) {
        outgoing.artifacts.clear()
        outgoing.artifact(shadowJar)
    }
}
