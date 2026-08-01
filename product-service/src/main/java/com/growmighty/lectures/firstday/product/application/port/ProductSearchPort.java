package com.growmighty.lectures.firstday.product.application.port;

import com.growmighty.lectures.firstday.product.application.dto.ProductSearchResult;

import java.util.List;

/**
 * 상품 검색 포트.
 * ★ application은 이 인터페이스에만 의존한다 — Elasticsearch 쿼리 DSL은 인프라 어댑터의 구현 세부사항 (DIP)
 */
public interface ProductSearchPort {

    List<ProductSearchResult> search(String keyword, Double minPrice, Double maxPrice, int page, int size);

    List<String> autocomplete(String prefix);

    List<ProductSearchResult> semanticSearch(String keyword, int size);   // ★ 오늘 추가

    List<ProductSearchResult> hybridSearch(String keyword, int size);     // ★ 오늘 추가 (구현은 3-3)

}
