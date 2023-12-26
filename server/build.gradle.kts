import java.sql.DriverManager
import java.sql.ResultSet

plugins {
    kotlin("jvm") version "1.8.10"
    application
    id("org.springframework.boot") version "3.0.0"
    id("org.liquibase.gradle") version "2.2.0"
}

group = "com.vl.messenger"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    /* Spring */
    implementation("org.springframework.boot:spring-boot-starter-web:3.0.0")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.0.0")
    implementation("org.springframework.boot:spring-boot-starter-security:3.0.0")
    implementation("org.springframework.boot:spring-boot-starter-websocket:3.0.0")
    /* JWT */
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    /* JDBC */
    implementation("mysql:mysql-connector-java:8.0.18")
    /* Liquibase */
    liquibaseRuntime("org.liquibase:liquibase-core:4.20.0")
    liquibaseRuntime("mysql:mysql-connector-java:8.0.18")
    liquibaseRuntime("info.picocli:picocli:4.6.3") // fixes NoClassDefFoundError
    /* JUnit */
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

liquibase {
    activities.register("main") {
        arguments = mapOf(
            "driver" to "com.mysql.cj.jdbc.Driver",
            "changelogFile" to "changelog.sql",
            "url" to (System.getenv("MSG_DB_URL")
                ?: throw IllegalArgumentException("Define environment variable MSG_DB_URL"))
        )
    }
}

tasks.getByName<Test>("test") {
    dependsOn(tasks.update)
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("mysql:mysql-connector-java:8.0.18")
    }
}

tasks.register("usersCount") {
    Class.forName("com.mysql.cj.jdbc.Driver")
    DriverManager.getConnection((System.getenv("MSG_DB_URL")
        ?: throw IllegalArgumentException("Define environment variable MSG_DB_URL")))
        .prepareStatement("select count(*) as count from user;").executeQuery()
        .also(ResultSet::next)
        .getInt("count")
        .also { println("There are $it users registered") }
}
