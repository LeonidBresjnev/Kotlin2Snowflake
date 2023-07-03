import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"

    id("org.jetbrains.kotlin.plugin.noarg") version "1.8.0"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.8.0"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.0"
    application
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}
group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.java.dev.jna:jna:5.12.1")
    implementation("net.java.dev.jna:jna-platform:5.12.1")
    implementation("net.snowflake:snowflake-jdbc:3.13.30")


    implementation("org.hibernate:hibernate-core:5.6.5.Final")
    testImplementation("org.hibernate:hibernate-testing:5.6.5.Final")

    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    implementation("com.github.gwenn:sqlite-dialect:0.1.2")
    implementation("org.flywaydb:flyway-core:8.4.3")
  //  implementation("com.github.gwenn:sqlite-dialect:1.1")
}



tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}