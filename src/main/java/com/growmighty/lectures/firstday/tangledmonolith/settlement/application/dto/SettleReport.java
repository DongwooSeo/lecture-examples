package com.growmighty.lectures.firstday.tangledmonolith.settlement.application.dto;

// 배치 테스트 리포트를 위한 DTO
public record SettleReport(
    long readCount,     // 메모리로 읽어들인 주문 수
    long settledCount,  // 실제 정산 생성 건수
    long elapsedMs,     // 소요 시간
    long peakHeapMb,    // 작업 중 피크 힙 사용량
    long maxHeapMb      // -Xmx 상한
) {
}
