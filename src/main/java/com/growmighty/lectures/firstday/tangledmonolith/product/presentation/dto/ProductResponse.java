package com.growmighty.lectures.firstday.tangledmonolith.product.presentation.dto;

import com.growmighty.lectures.firstday.tangledmonolith.product.application.dto.ProductInfo;

import java.math.BigDecimal;

/*
    HTTP 요청 -> RegisterProductRequest (PresentationDTO) -> RegisterProductCommand (Application DTO)
    -> Product (Entity) -> ProductInfo (Application DTO) -> ProductResponse (Presentation DTO)
    -> HTTP 응답
*/
public record ProductResponse(
        Long id,
        Long sellerId,
        String name,
        BigDecimal price,
        int stockQuantity,
        String status,
        boolean orderable
) {
    public static ProductResponse from(ProductInfo info) {
        return new ProductResponse(
                info.id(),
                info.sellerId(),
                info.name(),
                info.price(),
                info.stockQuantity(),
                info.status().name(),
                info.isOrderable()
        );
    }
}
