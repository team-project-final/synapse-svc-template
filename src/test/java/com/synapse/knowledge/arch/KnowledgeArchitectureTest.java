package com.synapse.knowledge.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class KnowledgeArchitectureTest {

    private static final String BASE = "com.synapse.knowledge";

    private static final JavaClasses CLASSES = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(BASE);

    // ─── 1. 도메인 격리 ─────────────────────────────────────────────────
    @Test
    void domain_slices_should_not_depend_on_each_other() {
        slices()
            .matching(BASE + ".(*)..")
            .namingSlices("$1")
            .should().notDependOnEachOther()
            .ignoreDependency(
                resideInAGlobalPackage(),
                anyClass()
            )
            .check(CLASSES);
    }

    // ─── 2~4. 계층 의존 방향 ────────────────────────────────────────────
    @Test
    void domain_should_not_depend_on_other_layers() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..api..", "..application..", "..infrastructure.."
            )
            .check(CLASSES);
    }

    @Test
    void application_should_not_depend_on_api_or_infrastructure() {
        noClasses()
            .that().resideInAPackage("..application..")
            .and().resideOutsideOfPackage("..application.port..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..api..", "..infrastructure.."
            )
            .check(CLASSES);
    }

    @Test
    void api_should_not_depend_on_infrastructure() {
        noClasses()
            .that().resideInAPackage("..api..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .check(CLASSES);
    }

    // ─── 5. domain.policy 외부 의존 0 ──────────────────────────────────
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

    // ─── 6. JpaRepository는 infrastructure.persistence에만 ────────────
    @Test
    void jpa_repositories_should_live_in_infrastructure_persistence() {
        classes()
            .that().areAssignableTo(org.springframework.data.jpa.repository.JpaRepository.class)
            .should().resideInAPackage("..infrastructure.persistence..")
            .check(CLASSES);
    }

    // ─── 7. @KafkaListener는 infrastructure.messaging에만 ─────────────
    @Test
    void kafka_listeners_should_live_in_infrastructure_messaging() {
        classes()
            .that().areAnnotatedWith(org.springframework.kafka.annotation.KafkaListener.class)
            .or().containAnyMethodsThat().areAnnotatedWith(org.springframework.kafka.annotation.KafkaListener.class)
            .should().resideInAPackage("..infrastructure.messaging..")
            .check(CLASSES);
    }

    // ─── 8. knowledge 특수: controller-less 도메인 허용 ────────────────
    // chunking은 api/ 패키지가 비어있어도 OK (kafka가 입구이기 때문).
    // 단 다른 도메인(note, graph)은 RestController가 반드시 있어야 함.
    @Test
    void non_pipeline_domains_should_have_controller() {
        classes()
            .that().resideInAnyPackage(BASE + ".note.api..", BASE + ".graph.api..")
            .and().haveSimpleNameEndingWith("Controller")
            .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .check(CLASSES);
    }

    // ─── helpers ──────────────────────────────────────────────────────
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
            public boolean test(com.tngtech.archunit.core.domain.JavaClass javaClass) {
                return true;
            }
        };
    }
}
