import com.google.protobuf.gradle.*

plugins {
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.11.1"
    id("org.jetbrains.kotlin.jvm") version "2.2.21"
    id("io.swagger.core.v3.swagger-gradle-plugin") version "2.2.34"
    id("org.flywaydb.flyway") version "11.11.1"
    id("com.diffplug.spotless") version "8.1.0"
    id("com.google.protobuf") version "0.9.5"
    java
}

group = "com.timor"
version = "0.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

extra["testcontainersVersion"] = "2.0.2"
extra["jjwtVersion"] = "0.12.6"

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.14")

    // Database
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
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "junit", module = "junit")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.3")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

    // Swagger
    implementation("io.swagger.core.v3:swagger-models:2.2.34")
    implementation("io.swagger.core.v3:swagger-core:2.2.34")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("com.sun.xml.bind:jaxb-impl:2.3.9")

    // gRPC and Protobuf
    implementation("io.grpc:grpc-netty-shaded:1.77.0")
    implementation("io.grpc:grpc-protobuf:1.77.0")
    implementation("io.grpc:grpc-stub:1.77.0")
    implementation("com.google.protobuf:protobuf-java:4.33.1")
    // Because protobuf is still using javax annotations
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE")
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
    archiveFileName.set("user-service.jar")
}

// Native image support for GraalVM
graalvmNative {
    binaries {
        named("main") {
            imageName.set("user-service")
            mainClass.set("com.timor.user.UserServiceApplication")
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
        googleJavaFormat("1.32.0").reflowLongStrings().formatJavadoc(true)

        leadingTabsToSpaces(1)
        formatAnnotations()
        trimTrailingWhitespace()
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
