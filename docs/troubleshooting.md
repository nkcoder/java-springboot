## JPA auditing

Move `@EnableJpaAuditing` to a separate configuration class `JpaAuditingConfig.java` so that we can exclude it from
testings. Otherwise it will throw errors when testing controllers:

```
Caused by: java.lang.IllegalArgumentException: JPA metamodel must not be empty
	at org.springframework.util.Assert.notEmpty(Assert.java:398)
```

## Rest Assured - not support Spring Boot 4

Rest Assured 5.5.6 doesn't work with Groovy 5 (still use Groovy 4), which is not compatible with Spring Boot 4(uses
Groovy 5).
We need to replace Rest Assured with `WebTestClient` before Rest Assured's upgrade.

- [Remove integration for REST Docs' REST Assured support until REST Assured supports Groovy 5](https://github.com/spring-projects/spring-boot/issues/47685)
- [Drop support for REST Assured until it supports Groovy 5](https://github.com/spring-projects/spring-restdocs/issues/1000)