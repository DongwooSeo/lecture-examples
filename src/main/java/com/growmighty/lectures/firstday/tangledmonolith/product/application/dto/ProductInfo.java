package com.growmighty.lectures.firstday.tangledmonolith.product.application.dto;

import com.growmighty.lectures.firstday.tangledmonolith.product.domain.Product;
import com.growmighty.lectures.firstday.tangledmonolith.product.domain.ProductStatus;

import java.math.BigDecimal;

/*
    과정 : DB -> Product(Entity) -> ProductInfo(Application DTO)
    -> Presentation DTO -> JSON
    ProductInfo는 Product를 외부에서 사용할 수 있도록 만든 읽기 전용 DTO이다.
    Entity를 그대로 변환하지 않고 Product -> ProductInfo로 변환해서 사용한다.
*/
public record ProductInfo(
        Long id,
        Long sellerId,
        String name,
        BigDecimal price,
        int stockQuantity,
        ProductStatus status
) {
    // Entity -> DTO 변환 메서드
    public static ProductInfo from(Product product) {
        return new ProductInfo(
                product.getId(),
                product.getSellerId(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus()
        );
    }

    // 순수하게 말하면 Entity에 메서드가 있는 것이 맞지만, 조회 전용 DTO에서도
    // 간단한 계산이나 편의 메서드는 넣어도 된다.
    public boolean isOrderable() {
        return this.status == ProductStatus.ON_SALE;
    }
}
