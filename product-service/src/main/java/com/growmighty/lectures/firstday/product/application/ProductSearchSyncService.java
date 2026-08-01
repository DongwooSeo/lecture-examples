package com.growmighty.lectures.firstday.product.application;

import com.growmighty.lectures.firstday.product.application.port.ProductIndexPort;
import com.growmighty.lectures.firstday.product.domain.Product;
import com.growmighty.lectures.firstday.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchSyncService {

    private final ProductRepository productRepository;
    private final ProductIndexPort indexPort;   // ★ 인프라(ProductSearchRepository)가 아닌 포트에 의존 (DIP)

    @Transactional(readOnly = true)
    public long reindexAll() {
        List<Product> products = productRepository.findAll();
        indexPort.indexAll(products);
        log.info("전체 재색인 완료: {}건", products.size());
        return products.size();
    }
}
