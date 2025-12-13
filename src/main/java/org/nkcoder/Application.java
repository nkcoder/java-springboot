package org.nkcoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.modulith.Modulith;

@Modulith(
        systemName = "Application",
        sharedModules = {"shared", "infrastructure"})
@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {
    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
