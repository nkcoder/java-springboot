import com.google.protobuf.gradle.id

plugins {
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.11.1"
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    id("org.flywaydb.flyway") version "11.11.1"
    id("com.diffplug.spotless") version "8.1.0"
    id("com.google.protobuf") version "0.9.5"
    java
    jacoco
}

group = "org.nkcoder"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

extra["testcontainersVersion"] = "1.21.3"
extra["jjwtVersion"] = "0.13.0"

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jackson")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")

    // Database
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    runtimeOnly("org.postgresql:postgresql:42.7.8")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:11.11.1")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:${property("jjwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jjwtVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jjwtVersion")}")

    // Development
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")  // For WebTestClient
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.3")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

    // gRPC and Protobuf
    implementation("io.grpc:grpc-netty-shaded:1.77.0")
    implementation("io.grpc:grpc-protobuf:1.77.0")
    implementation("io.grpc:grpc-stub:1.77.0")
    implementation("com.google.protobuf:protobuf-java:4.33.1")
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
    implementation("org.springframework.grpc:spring-grpc-spring-boot-starter:1.0.0")
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// set up the output JAR name
tasks.bootJar {
    archiveFileName.set("user-application.jar")
}

// Native image support for GraalVM
graalvmNative {
    binaries {
        named("main") {
            imageName.set("user-service")
            mainClass.set("org.nkcoder.UserApplication")
            debug.set(false)
        }
    }
}

// Custom tasks for microservice operations
tasks.register("buildImage") {
    dependsOn("bootBuildImage")
    description = "Build Docker image using Spring Boot buildpacks"
}

tasks.register("runLocal") {
    dependsOn("bootRun")
    description = "Run the application locally with development profile"
}

// JVM optimization for microservices
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs = listOf(
        "-Xms256m", "-Xmx512m", "-XX:+UseG1GC", "-XX:+UseStringDeduplication"
    )
}


// spotless configuration for code formatting
spotless {
    java {
        importOrder()
        removeUnusedImports()

        // Choose one formatters: google or palantir
        palantirJavaFormat().formatJavadoc(true)
        formatAnnotations()
        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()

        target("src/**/*.java")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.33.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.77.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}
sourceSets {
    main {
        java {
            srcDirs("buf-gen/generated/sources/proto/main/java")
            srcDirs("buf-gen/generated/sources/proto/main/grpc")
        }
    }
}

tasks.test {
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    failFast = true

    // Cache and incremental test execution
    outputs.cacheIf { true }
}

// Test coverage configuration (JaCoCo)
apply(from = "gradle/jacoco.gradle.kts")
