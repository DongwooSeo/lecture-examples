package com.growmighty.lectures.firstday.tangledmonolith.settlement.domain;

public enum SettlementStatus {
    PENDING, // 확정 전, 정산 대상
    COMPLETED, // 정산 확정 완료
    FAILED // 정산 처리 실패
}
