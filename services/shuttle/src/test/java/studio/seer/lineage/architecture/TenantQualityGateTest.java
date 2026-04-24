package studio.seer.lineage.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * SHT-16: QG-SHUTTLE-tenant quality gate.
 *
 * All rules must be GREEN before the sprint is considered complete.
 *
 * Rules:
 *  QG-1  No raw System.getenv() for config (enforced by ShuttleArchitectureTest)
 *  QG-2  Resources must not create their own ArcadeGateway — must inject it
 *  QG-3  Security classes are not bypassed by client adapters
 *  QG-4  Service classes must reside only in the service package
 *  QG-5  All @GraphQLApi classes are in the resource package
 */
class TenantQualityGateTest {

    private static final JavaClasses SHUTTLE_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("studio.seer.lineage");

    /** QG-2: Resources must not instantiate ArcadeGateway directly — always inject. */
    @Test
    void qg2_resourcesDoNotInstantiateArcadeGateway() {
        noClasses()
                .that().resideInAPackage("studio.seer.lineage.resource..")
                .should().callConstructor(
                        studio.seer.lineage.client.ArcadeGateway.class)
                .because("ArcadeGateway must be injected via CDI, not newed directly")
                .check(SHUTTLE_CLASSES);
    }

    /** QG-3: Client adapters must not access the security package. */
    @Test
    void qg3_clientAdaptersDoNotAccessSecurity() {
        noClasses()
                .that().resideInAPackage("studio.seer.lineage.client..")
                .should().dependOnClassesThat()
                .resideInAPackage("studio.seer.lineage.security..")
                .because("Client adapters must not bypass the identity layer")
                .check(SHUTTLE_CLASSES);
    }

    /** QG-4: Classes annotated @ApplicationScoped in services stay in the service package. */
    @Test
    void qg4_applicationScopedServicesInServicePackage() {
        classes()
                .that().areAnnotatedWith(jakarta.enterprise.context.ApplicationScoped.class)
                .and().haveNameMatching(".*Service")
                .should().resideInAPackage("studio.seer.lineage.service..")
                .because("Service classes must live in the service package for layering clarity")
                .check(SHUTTLE_CLASSES);
    }

    /** QG-5: @GraphQLApi classes must be in the resource package. */
    @Test
    void qg5_graphQlApiInResourcePackage() {
        classes()
                .that().areAnnotatedWith(org.eclipse.microprofile.graphql.GraphQLApi.class)
                .should().resideInAPackage("studio.seer.lineage.resource..")
                .because("All GraphQL API endpoints must live in the resource package")
                .check(SHUTTLE_CLASSES);
    }
}
