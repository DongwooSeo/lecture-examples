package com.growmighty.lectures.firstday.product.application.port;

import com.growmighty.lectures.firstday.product.domain.Product;

import java.util.List;

/**
 * 상품 색인 포트.
 * ★ 도메인 모델(Product)을 받는다 — ProductDocument 변환은 인프라 어댑터의 책임 (DIP)
 */
public interface ProductIndexPort {

    void indexAll(List<Product> products);
}
