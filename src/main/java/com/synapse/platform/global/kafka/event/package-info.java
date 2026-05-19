/**
 * 임시 stub 이벤트 클래스.
 *
 * synapse-shared 멀티모듈(shared-events)이 publish된 후 이 패키지는 삭제하고,
 * 각 producer/consumer에서 com.synapse.shared.event.* import로 교체해야 합니다.
 *
 * 마이그레이션 체크리스트:
 * 1. synapse-shared:shared-events 1.0.0 publish
 * 2. build.gradle.kts에 implementation("com.synapse:shared-events:1.0.0") 활성화
 * 3. 이 패키지(global/kafka/event)의 record들을 shared-events 클래스로 import 교체
 * 4. 이 패키지 삭제
 */
package com.synapse.platform.global.kafka.event;
