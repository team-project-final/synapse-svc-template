plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.synapse"
version = "0.3.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    // GitHub Packages — synapse-shared 배포처
    // maven {
    //     url = uri("https://maven.pkg.github.com/team-project-final/synapse-shared")
    //     credentials { ... }
    // }
}

dependencies {
    // Web / JPA / Validation
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Security + JWT (W2)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Redis (W2)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // W3 NEW — Kafka + Avro
    implementation("org.springframework.kafka:spring-kafka")
    // implementation("com.synapse:shared-events:1.0.0")  // synapse-shared 멀티모듈 publish 후 활성화
    // 임시: 로컬 stub 이벤트 클래스(global/kafka/event/*) 사용

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
