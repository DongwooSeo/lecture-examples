package com.growmighty.lectures.firstday.product.application.dto;

import com.growmighty.lectures.firstday.product.domain.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * 검색 결과용 조회 모델.
 * ★ 인프라의 ProductDocument를 application/presentation으로 노출하지 않기 위한 DTO
 *   (embedding 벡터는 내부 색인용이므로 응답에 포함하지 않는다)
 */
public record ProductSearchResult(
        Long id,
        Long sellerId,
        String name,
        String description,
        BigDecimal price,
        ProductStatus status,
        long salesCount,
        List<String> occasions,
        List<String> styles,
        List<String> seasons,
        String material,
        boolean rainFriendly
) {
}
