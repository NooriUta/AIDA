package studio.seer.heimdall;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * R-ARCH-07 / R-ARCH-05 — ArchUnit enforcement for the HEIMDALL backend service.
 *
 * <p>Heimdall is the event bus for AIDA. It must:
 * <ul>
 *   <li>Not read environment variables directly (config via {@code @ConfigProperty}).</li>
 *   <li>Not depend on other AIDA service packages (shuttle, dali, anvil) — services
 *       communicate via events, not direct imports.</li>
 *   <li>Not access ArcadeDB driver directly (all data flows through ArcadeGateway).</li>
 * </ul>
 */
@AnalyzeClasses(
        packages = "studio.seer.heimdall",
        importOptions = ImportOption.DoNotIncludeTests.class)
public class HeimdallArchitectureTest {

    /**
     * R-ARCH-07 — no direct {@code System.getenv()} anywhere in heimdall-backend.
     */
    @ArchTest
    static final ArchRule no_direct_system_getenv =
            noClasses()
                    .that().resideInAPackage("studio.seer.heimdall..")
                    .should().callMethod(System.class, "getenv", String.class)
                    .because("R-ARCH-07: env var reads must go through @ConfigProperty, " +
                             "not raw System.getenv()");

    /**
     * R-ARCH-07 (secondary) — no raw {@code System.getProperty()} in heimdall-backend.
     */
    @ArchTest
    static final ArchRule no_direct_system_getproperty =
            noClasses()
                    .that().resideInAPackage("studio.seer.heimdall..")
                    .should().callMethod(System.class, "getProperty", String.class)
                    .orShould().callMethod(System.class, "getProperty", String.class, String.class)
                    .because("R-ARCH-07: system property reads must go through @ConfigProperty");

    /**
     * R-ARCH-05 — Heimdall must not import other AIDA service packages directly.
     * Service-to-service communication is via HEIMDALL events (fire-and-forget),
     * not by calling other services' classes.
     */
    @ArchTest
    static final ArchRule no_direct_service_dependencies =
            noClasses()
                    .that().resideInAPackage("studio.seer.heimdall..")
                    .should().dependOnClassesThat()
                        .resideInAnyPackage(
                                "studio.seer.dali..",
                                "studio.seer.lineage..",   // shuttle
                                "studio.seer.anvil..")
                    .because("R-ARCH-05: Heimdall must be decoupled from other services — " +
                             "integrate via events only, not direct class imports");
}
