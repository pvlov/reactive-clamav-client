plugins {
    id("java")
    libs.plugins.jmh
}

group = "pvlov"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.slf4j.simple)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.jmh.core)
    testImplementation(libs.jmh.annotations)
    testImplementation(libs.reactor.test)
    testImplementation(libs.test.containers)
    testImplementation(libs.test.containers.junit)
}

tasks.test {
    useJUnitPlatform()
}