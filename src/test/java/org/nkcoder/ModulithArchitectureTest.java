package org.nkcoder;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModulithArchitectureTest {
    ApplicationModules modules = ApplicationModules.of(Application.class);

    @Test
    void verifyModuleStructure() {
        // Verifies no cyclic dependencies and proper encapsulation
        modules.verify();
    }

    @Test
    void printModuleArrangement() {
        modules.forEach(System.out::println);
    }

    @Test
    void generateDocumentation() {
        new Documenter(modules).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml();
    }
}
