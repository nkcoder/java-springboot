package org.nkcoder.infrastructure.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Note: @SpringBootTest performs full component scanning and will include the {@link
 * JpaAuditingConfig} and enable JPA auditing. So don't add @EnableJPAAuditing on this annotation,
 * otherwise it will be registered multiple times during AOT processing.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfiguration.class)
@ActiveProfiles("test")
@Transactional // Auto-rollback after each test
public @interface IntegrationTest {}
