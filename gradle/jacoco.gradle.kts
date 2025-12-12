/**
 * JaCoCo test coverage configuration.
 *
 * This file configures code coverage reporting and verification thresholds.
 * Apply this script in build.gradle.kts with: apply(from = "gradle/jacoco.gradle.kts")
 */

// JaCoCo plugin configuration
configure<JacocoPluginExtension> {
    toolVersion = "0.8.14"
}

// Shared exclusion patterns for classes that don't need coverage
val jacocoExclusions = listOf(
    // Top-level infrastructure config (Spring wiring, no business logic)
    "**/infrastructure/config/**",
    "**/infrastructure/resolver/**",
    // DTOs, commands, requests, responses (data carriers, no logic)
    "**/dto/**",
    "**/request/**",
    "**/response/**",
    "**/entity/**",
    // Mappers (simple transformations)
    "**/mapper/**",
    // Domain value objects (simple wrappers with no business logic)
    "**/domain/model/*Id.class",
    "**/domain/model/*Role.class",
    "**/domain/model/TokenFamily.class",
    "**/domain/model/TokenPair.class",
    "**/domain/model/HashedPassword.class",
    // Domain events (data carriers)
    "**/domain/event/**",
    // Domain repository interfaces (just interfaces, no implementation)
    "**/domain/repository/**",
    // Domain service interfaces (PasswordEncoder, TokenGenerator - just interfaces)
    "**/domain/service/PasswordEncoder.class",
    "**/domain/service/TokenGenerator*.class",
    // Shared kernel (framework utilities)
    "**/shared/**",
    // Application entry point
    "**/*Application.class",
    // Generated code (protobuf, grpc)
    "**/proto/**",
    "**/generated/**"
)

// Classes that require strict 80% coverage (core business logic)
val strictCoverageClasses = listOf(
    // Application services - orchestration logic
    "org.nkcoder.user.application.service.*",
    // Domain services - business logic
    "org.nkcoder.user.domain.service.AuthenticationService",
    "org.nkcoder.user.domain.service.TokenRotationService",
    // Domain model - core business rules
    "org.nkcoder.user.domain.model.User",
    "org.nkcoder.user.domain.model.RefreshToken",
    "org.nkcoder.user.domain.model.Email",
    "org.nkcoder.user.domain.model.UserName",
    // Infrastructure - repository persistence
    "org.nkcoder.user.infrastructure.persistence.repository",
)

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"), tasks.named("processResources"), tasks.named("compileJava"))
    reports {
        xml.required.set(true)      // For CI/CD integration
        html.required.set(true)     // For human-readable reports
        csv.required.set(false)
    }

    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(jacocoExclusions)
            }
        })
    )
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("jacocoTestReport"))
    violationRules {
        // Global minimum
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
        // Strict requirements for core business logic (domain + application layers)
        rule {
            element = "CLASS"
            includes = strictCoverageClasses
            limit {
                minimum = "0.90".toBigDecimal()
            }
        }
        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude(jacocoExclusions)
                }
            })
        )
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
