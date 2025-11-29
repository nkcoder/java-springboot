## JPA auditing

Move `@EnableJpaAuditing` to a separate configuration class `JpaAuditingConfig.java` so that we can exclude it from
testings. Otherwise it will throw errors when testing controllers:
```
Caused by: java.lang.IllegalArgumentException: JPA metamodel must not be empty
	at org.springframework.util.Assert.notEmpty(Assert.java:398)
```
