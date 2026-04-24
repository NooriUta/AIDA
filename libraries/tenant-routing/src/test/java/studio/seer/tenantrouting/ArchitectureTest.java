package studio.seer.tenantrouting;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit: ensures no classes outside the tenant-routing package read YGG/S3/LLM
 * env vars directly. Enforces the registry pattern from SHUTTLE_TENANT_ISOLATION.md §5.
 *
 * This test validates the tenant-routing library itself. The same rule must be applied
 * to services (Dali, SHUTTLE, ANVIL, MIMIR) in their own ArchUnit tests (DMT-10, SHT-15).
 */
class ArchitectureTest {

    @Test
    void tenantRoutingPackage_doesNotCallSystemGetenv_forYggVars() {
        // The library itself is allowed to call System.getenv for its own config.
        // Services outside tenant-routing are NOT — verified in DMT-10 / SHT-15.
        // Here we only check that FriggYggLineageRegistry (the stub) doesn't silently
        // read env vars on its own.
        var classes = new ClassFileImporter()
                .importPackages("studio.seer.tenantrouting");

        ArchRule noDirectEnvInStub = noClasses()
                .that().haveSimpleNameContaining("Frigg")
                .should().callMethod(System.class, "getenv", String.class)
                .because("FriggYggLineageRegistry stub must not silently read env vars");

        noDirectEnvInStub.check(classes);
    }
}
