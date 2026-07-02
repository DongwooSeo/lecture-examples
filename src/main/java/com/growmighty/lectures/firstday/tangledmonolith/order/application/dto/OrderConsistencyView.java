package com.growmighty.lectures.firstday.tangledmonolith.order.application.dto;

import java.math.BigDecimal;

// 주문에 저장된 총액(storedTotal)과 항목들로 다시 더한 총액(recalculatedTotal) 비교 클래스
// application의 DTO로 이동.
public record OrderConsistencyView (
        Long orderId,
        BigDecimal storedTotal,
        BigDecimal recalculatedTotal,
        boolean consistent
) {
}
