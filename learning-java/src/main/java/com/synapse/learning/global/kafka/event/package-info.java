/**
 * 임시 stub 이벤트 클래스 (Java 쪽).
 *
 * synapse-shared 멀티모듈(shared-events)이 publish된 후 이 패키지는 삭제하고,
 * 각 producer/consumer/service에서 com.synapse.shared.event.* import로 교체해야 합니다.
 *
 * Python 쪽도 동일한 마이그레이션 — learning-ai/app/kafka/events.py 에서 shared로 이전 예정.
 *
 * 마이그레이션 체크리스트:
 * 1. synapse-shared:shared-events 1.0.0 publish
 * 2. learning-java/build.gradle.kts에 implementation("com.synapse:shared-events:1.0.0") 활성화
 * 3. 이 패키지의 record들을 shared-events 클래스로 import 교체
 * 4. 이 패키지 삭제
 * 5. Python 쪽 events.py도 shared-events Avro 생성 클래스로 교체
 */
package com.synapse.learning.global.kafka.event;
