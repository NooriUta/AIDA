package studio.seer.anvil;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * R-ARCH-01 / R-ARCH-07 — ArchUnit enforcement for the ANVIL service.
 *
 * <p>Mirrors the same rules in {@code DaliArchitectureTest} and
 * {@code ShuttleArchitectureTest} — enforcing that Anvil follows the
 * same constraints as all other AIDA services:
 * <ul>
 *   <li>No direct reading of environment variables outside config layer.</li>
 *   <li>No raw {@link System#getProperty} calls that could bypass typed config.</li>
 * </ul>
 *
 * <p>Additional rule: Anvil must NOT directly access ArcadeDB driver classes
 * ({@code com.arcadedb.database.*}) — all DB access must go through
 * the tenant-routing {@code YggLineageRegistry} / {@code ArcadeGateway}.
 */
@AnalyzeClasses(
        packages = "studio.seer.anvil",
        importOptions = ImportOption.DoNotIncludeTests.class)
public class AnvilArchitectureTest {

    /**
     * R-ARCH-07 / ADR-CONFIG-001 — config reads must go through {@code @ConfigProperty},
     * not raw {@code System.getenv()} calls that bypass Quarkus config.
     */
    @ArchTest
    static final ArchRule no_direct_system_getenv =
            noClasses()
                    .that().resideInAPackage("studio.seer.anvil..")
                    .should().callMethod(System.class, "getenv", String.class)
                    .because("R-ARCH-07: env var reads must go through @ConfigProperty — " +
                             "direct System.getenv() bypasses config layer");

    /**
     * R-ARCH-07 (secondary) — {@code System.getProperty()} is equally banned.
     */
    @ArchTest
    static final ArchRule no_direct_system_getproperty =
            noClasses()
                    .that().resideInAPackage("studio.seer.anvil..")
                    .should().callMethod(System.class, "getProperty", String.class)
                    .orShould().callMethod(System.class, "getProperty", String.class, String.class)
                    .because("R-ARCH-07: system property reads must go through @ConfigProperty");

    /**
     * R-ARCH-01 — Anvil must not directly instantiate or reference ArcadeDB
     * low-level driver classes. All database interactions must go through the
     * abstraction layer ({@code ArcadeGateway} or {@code YggLineageRegistry}).
     */
    @ArchTest
    static final ArchRule no_direct_arcadedb_driver =
            noClasses()
                    .that().resideInAPackage("studio.seer.anvil..")
                    .should().dependOnClassesThat()
                        .resideInAnyPackage(
                                "com.arcadedb.database..",
                                "com.arcadedb.remote..",
                                "com.arcadedb.query.sql.executor..")
                    .because("R-ARCH-01 / MTN-02: Anvil must access ArcadeDB only through " +
                             "ArcadeGateway — direct driver access bypasses tenant routing");
}
