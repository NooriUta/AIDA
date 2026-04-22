package studio.seer.lineage.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import org.junit.jupiter.api.Test;
import studio.seer.lineage.client.ArcadeGateway;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * MTN-02: Compile-time gate against tenant-unaware ArcadeGateway calls.
 *
 * <p>Rule: no class in {@code service.*} / {@code resource.*} / {@code storage.*}
 * (and any other domain layer) may call the deprecated {@link ArcadeGateway#sql}
 * / {@link ArcadeGateway#cypher} overloads that lack an explicit {@code database}
 * parameter — those route to the default DB and leak across tenants.
 *
 * <p>Existing violations are captured in
 * {@code src/test/resources/archunit_store/} via {@link FreezingArchRule}. The
 * build passes initially but fails when a NEW call to the deprecated API
 * appears in a class that wasn't previously frozen. Migrating a class to
 * {@code sqlIn / cypherIn} removes it from the frozen set, raising the bar
 * monotonically.
 */
class TenantQueryGateTest {

    private static final JavaClasses SHUTTLE_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("studio.seer.lineage");

    @Test
    void noNewCallsToTenantUnawareArcadeGateway() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "studio.seer.lineage.service..",
                        "studio.seer.lineage.resource..",
                        "studio.seer.lineage.storage..")
                .should().callMethod(ArcadeGateway.class, "sql",    String.class)
                .orShould().callMethod(ArcadeGateway.class, "sql",    String.class, java.util.Map.class)
                .orShould().callMethod(ArcadeGateway.class, "cypher", String.class)
                .orShould().callMethod(ArcadeGateway.class, "cypher", String.class, java.util.Map.class)
                .because("MTN-02: use sqlIn/cypherIn with per-tenant dbName resolved " +
                         "via YggLineageRegistry.resourceFor(alias).databaseName() — " +
                         "tenant-unaware overloads are @Deprecated(forRemoval)");

        // Freeze current violations; new ones will fail the build. To lower the
        // baseline after migrating a class, delete its entries from
        // src/test/resources/archunit_store/ and re-run.
        FreezingArchRule.freeze(rule).check(SHUTTLE_CLASSES);
    }
}
