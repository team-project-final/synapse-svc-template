package com.synapse.platform.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class PlatformArchitectureTest {

    private static final String BASE = "com.synapse.platform";

    private static final JavaClasses CLASSES = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(BASE);

    // ─── 1. 도메인 격리 ─────────────────────────────────────────────────
    // auth/, audit/, billing/, notification/ 슬라이스 간 직접 의존 금지
    // (target이 global/이거나 source가 global/인 의존은 모두 허용 — global은 횡단 인프라)
    @Test
    void domain_slices_should_not_depend_on_each_other() {
        slices()
            .matching(BASE + ".(*)..")
            .namingSlices("$1")
            .should().notDependOnEachOther()
            // global은 횡단 — source/target 어느 쪽이든 허용
            .ignoreDependency(anyClass(), resideInAGlobalPackage())
            .ignoreDependency(resideInAGlobalPackage(), anyClass())
            .check(CLASSES);
    }

    // ─── 2. 계층 의존 방향 ──────────────────────────────────────────────
    // api → application → domain ← infrastructure
    @Test
    void domain_should_not_depend_on_application_api_infrastructure() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..api..", "..application..", "..infrastructure.."
            )
            .check(CLASSES);
    }

    @Test
    void application_should_not_depend_on_api_non_dto_or_infrastructure() {
        // application은 api/dto/ (입출력 DTO)는 사용 가능하지만 controller나 infrastructure는 금지.
        // dto는 application과 api가 공유하는 데이터 계약 (헥사고날 라이트 합의).
        com.tngtech.archunit.base.DescribedPredicate<com.tngtech.archunit.core.domain.JavaClass> apiNonDto =
            com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage("..api..")
                .and(com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage("..api.dto.."));
        com.tngtech.archunit.base.DescribedPredicate<com.tngtech.archunit.core.domain.JavaClass> forbidden =
            apiNonDto.or(com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage("..infrastructure.."));

        noClasses()
            .that().resideInAPackage("..application..")
            .and().resideOutsideOfPackage("..application.port..")
            .should().dependOnClassesThat(forbidden)
            .check(CLASSES);
    }

    @Test
    void api_should_not_depend_on_infrastructure() {
        noClasses()
            .that().resideInAPackage("..api..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .check(CLASSES);
    }

    // ─── 3. domain.policy는 외부 의존성 0 ─────────────────────────────
    // 도메인 룰 변경이 인프라/API 변경과 분리되어야 함
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

    // ─── 4. JPA repository는 infrastructure/persistence에만 ────────────
    @Test
    void jpa_repositories_should_live_in_infrastructure_persistence() {
        classes()
            .that().areAssignableTo(org.springframework.data.jpa.repository.JpaRepository.class)
            .should().resideInAPackage("..infrastructure.persistence..")
            .check(CLASSES);
    }

    // ─── 5. Kafka @KafkaListener는 infrastructure/messaging에만 ────────
    @Test
    void kafka_listeners_should_live_in_infrastructure_messaging() {
        classes()
            .that().areAnnotatedWith(org.springframework.kafka.annotation.KafkaListener.class)
            .or().containAnyMethodsThat(annotatedWithKafkaListener())
            .should().resideInAPackage("..infrastructure.messaging..")
            .check(CLASSES);
    }

    private static com.tngtech.archunit.base.DescribedPredicate<com.tngtech.archunit.core.domain.JavaMethod> annotatedWithKafkaListener() {
        return new com.tngtech.archunit.base.DescribedPredicate<>("annotated with @KafkaListener") {
            @Override
            public boolean test(com.tngtech.archunit.core.domain.JavaMethod method) {
                return method.isAnnotatedWith(org.springframework.kafka.annotation.KafkaListener.class);
            }
        };
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
