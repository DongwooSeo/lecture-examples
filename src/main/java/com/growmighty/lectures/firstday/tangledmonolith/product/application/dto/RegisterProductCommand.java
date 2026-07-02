package com.growmighty.lectures.firstday.tangledmonolith.product.application.dto;

import java.math.BigDecimal;

/*
    RegisterProductCommand는 비즈니스 객체가 아니라 데이터 전달만 한다.
    현재 이 Command 객체는 상품 등록을 해달라는 요청을 표현하는 객체다.
    HTTP 요청 -> Controller -> RegisterProductCommand -> Application Service

*/
public record RegisterProductCommand(
        Long sellerId,
        String name,
        BigDecimal price,
        int stockQuantity,
        String description
) {
}
