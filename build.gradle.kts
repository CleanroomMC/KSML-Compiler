plugins {
    id("java")
    antlr
    id("com.gradleup.shadow") version ("8.3.0")
    application
}

group = "com.cleanroommc.ksmlc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    shadow("org.antlr:antlr4-runtime:4.5") {
        isTransitive = false
    }
    antlr("org.antlr:antlr4:4.5")

    implementation("org.anarres:jcpp:1.4.14")
    shadow("org.anarres:jcpp:1.4.14") {
        isTransitive = false
    }

    implementation("com.github.ChAoSUnItY:Nenggao:1.4.3")
    
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.generateGrammarSource {
    arguments.addAll(listOf("-visitor", "-package", "com.cleanroommc.ksmlc.glsl.grammar"))
    outputDirectory = file("build/generated-src/antlr/main/com/cleanroommc/ksmlc/glsl/grammar/")
}

application {
    mainClass = "com.cleanroommc.ksmlc.Main"
}

tasks.test {
    useJUnitPlatform()
}
