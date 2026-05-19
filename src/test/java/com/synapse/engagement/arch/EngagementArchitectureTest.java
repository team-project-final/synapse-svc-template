package com.synapse.engagement.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class EngagementArchitectureTest {

    private static final String BASE = "com.synapse.engagement";

    private static final JavaClasses CLASSES = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(BASE);

    @Test
    void domain_slices_should_not_depend_on_each_other() {
        slices()
            .matching(BASE + ".(*)..")
            .namingSlices("$1")
            .should().notDependOnEachOther()
            .ignoreDependency(resideInAGlobalPackage(), anyClass())
            .check(CLASSES);
    }

    @Test
    void domain_should_not_depend_on_other_layers() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..api..", "..application..", "..infrastructure..")
            .check(CLASSES);
    }

    @Test
    void application_should_not_depend_on_api_or_infrastructure() {
        noClasses()
            .that().resideInAPackage("..application..")
            .and().resideOutsideOfPackage("..application.port..")
            .should().dependOnClassesThat().resideInAnyPackage("..api..", "..infrastructure..")
            .check(CLASSES);
    }

    @Test
    void api_should_not_depend_on_infrastructure() {
        noClasses()
            .that().resideInAPackage("..api..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .check(CLASSES);
    }

    /**
     * engagement 강조: domain.policy는 비즈니스 룰의 핵심.
     * 외부 의존성 0을 엄격히 강제.
     */
    @Test
    void domain_policy_should_have_no_outside_dependencies() {
        classes()
            .that().resideInAPackage("..domain.policy..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                "java..",
                "..domain.policy..",
                "..domain.."
            )
            .check(CLASSES);
    }

    @Test
    void jpa_repositories_should_live_in_infrastructure_persistence() {
        classes()
            .that().areAssignableTo(org.springframework.data.jpa.repository.JpaRepository.class)
            .should().resideInAPackage("..infrastructure.persistence..")
            .check(CLASSES);
    }

    @Test
    void kafka_listeners_should_live_in_infrastructure_messaging() {
        classes()
            .that().areAnnotatedWith(org.springframework.kafka.annotation.KafkaListener.class)
            .or().containAnyMethodsThat().areAnnotatedWith(org.springframework.kafka.annotation.KafkaListener.class)
            .should().resideInAPackage("..infrastructure.messaging..")
            .check(CLASSES);
    }

    private static com.tngtech.archunit.base.DescribedPredicate<com.tngtech.archunit.core.domain.JavaClass> resideInAGlobalPackage() {
        return new com.tngtech.archunit.base.DescribedPredicate<>("reside in ..global..") {
            @Override
            public boolean test(com.tngtech.archunit.core.domain.JavaClass javaClass) {
                return javaClass.getPackageName().contains(".global.");
            }
        };
    }

    private static com.tngtech.archunit.base.DescribedPredicate<com.tngtech.archunit.core.domain.JavaClass> anyClass() {
        return new com.tngtech.archunit.base.DescribedPredicate<>("any class") {
            @Override
            public boolean test(com.tngtech.archunit.core.domain.JavaClass javaClass) { return true; }
        };
    }
}
