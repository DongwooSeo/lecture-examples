package com.growmighty.lectures.firstday.tangledmonolith.product.presentation.dto;

import com.growmighty.lectures.firstday.tangledmonolith.product.application.dto.RegisterProductCommand;
import lombok.NonNull;

import java.math.BigDecimal;

public record RegisterProductRequest(
        // Presentation 계층으로 들어오는 요청이 null이면 안 된다.
        // Product는 create를 통해 생성되고 그 안에 검증 장치가 다 있으니 안 해도 됨.
        // 반면 Request는 API 요청이고 DTO밖에 검증 수단이 없어 여기서 NonNull 조치를 취함.
        @NonNull Long sellerId,
        @NonNull String name,
        @NonNull BigDecimal price,
        @NonNull Integer stockQuantity,
        String description
) {
        public RegisterProductCommand toCommand() {
                return new RegisterProductCommand(sellerId, name, price, stockQuantity, description);
        }
}
