package studio.seer.lineage.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * SHT-15: ArchUnit rules for Shuttle.
 *
 * Enforces that no class outside :libraries:tenant-routing reads YGG config
 * directly from environment variables or system properties.
 */
class ShuttleArchitectureTest {

    private static final JavaClasses SHUTTLE_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("studio.seer.lineage");

    @Test
    void noDirectEnvReadsForArcadeConfig() {
        noClasses()
                .that().resideInAPackage("studio.seer.lineage..")
                .should().callMethod(System.class, "getenv", String.class)
                .because("ArcadeDB config must flow through YggLineageRegistry / @ConfigProperty")
                .check(SHUTTLE_CLASSES);
    }

    @Test
    void noDirectSystemPropertiesForArcadeConfig() {
        noClasses()
                .that().resideInAPackage("studio.seer.lineage..")
                .should().callMethod(System.class, "getProperty", String.class)
                .because("Config must come from Quarkus @ConfigProperty, not System.getProperty")
                .check(SHUTTLE_CLASSES);
    }

    @Test
    void clientPackageDoesNotAccessSecurityDirectly() {
        // Client adapters should not bypass the identity layer by reading headers themselves
        noClasses()
                .that().resideInAPackage("studio.seer.lineage.client..")
                .should().dependOnClassesThat()
                .resideInAPackage("studio.seer.lineage.security..")
                .because("Client adapters get tenant context injected via headers, not from SeerIdentity")
                .check(SHUTTLE_CLASSES);
    }
}
