package org.nkcoder.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

  @Bean
  public TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }

  @Bean
  public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
    return registry ->
        registry
            .config()
            .commonTags(
                "application",
                "user-service",
                "environment",
                System.getProperty("spring.profiles.active", "unknown"))
            .meterFilter(
                MeterFilter.deny(
                    id -> {
                      String uri = id.getTag("uri");
                      return uri != null
                          && (uri.startsWith("/actuator") || uri.startsWith("/swagger"));
                    }));
  }
}
