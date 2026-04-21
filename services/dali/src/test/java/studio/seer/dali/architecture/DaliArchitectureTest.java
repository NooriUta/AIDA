package studio.seer.dali.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * DMT-10: ArchUnit rules for Dali service.
 *
 * Rule: no class in studio.seer.dali.** reads System.getenv() for YGG_*, S3_*, CLAUDE_*
 * environment variables directly. All environment-sourced config must go through
 * :libraries:tenant-routing (YggLineageRegistry / YggSourceArchiveRegistry) or
 * Quarkus @ConfigProperty / DaliConfig.
 */
class DaliArchitectureTest {

    private static final JavaClasses DALI_CLASSES = new ClassFileImporter()
            .importPackages("studio.seer.dali");

    @Test
    void noDirectEnvReadsForYggConfig() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("studio.seer.dali..")
                .should().callMethod(System.class, "getenv", String.class)
                .as("Dali classes must not call System.getenv() directly — use @ConfigProperty or tenant-routing registries");

        rule.check(DALI_CLASSES);
    }

    @Test
    void noDirectSystemPropertiesForYggConfig() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("studio.seer.dali..")
                .should().callMethod(System.class, "getProperty", String.class)
                .as("Dali classes must not call System.getProperty() — use @ConfigProperty");

        rule.check(DALI_CLASSES);
    }
}
