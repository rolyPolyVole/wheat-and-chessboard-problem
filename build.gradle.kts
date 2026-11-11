plugins {
    kotlin("jvm") version "2.4.21"
    id("net.fabricmc.fabric-loom") version "1.19-SNAPSHOT"
}

group = "dev.rolypolyvole"
version = "${project.property("mod_version")}+${project.property("minecraft_version")}"

base {
    archivesName.set(project.property("archives_base_name") as String)
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    implementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")

    implementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    implementation("com.ezylang:EvalEx:${project.property("evalex_version")}")
    include("com.ezylang:EvalEx:${project.property("evalex_version")}")

    testImplementation(kotlin("test"))
}

tasks.processResources {
    val properties = mapOf(
        "version" to project.version,
        "minecraft_version" to project.property("minecraft_version"),
        "loader_version" to project.property("loader_version"),
        "kotlin_loader_version" to project.property("kotlin_loader_version")
    )

    inputs.properties(properties)
    filesMatching("fabric.mod.json") { expand(properties) }
}

tasks.jar {
    from("LICENSE.txt") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}

tasks.test {
    useJUnitPlatform()
}
