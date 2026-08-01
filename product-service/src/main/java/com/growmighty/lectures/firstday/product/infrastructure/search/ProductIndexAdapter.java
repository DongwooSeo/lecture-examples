package com.growmighty.lectures.firstday.product.infrastructure.search;

import com.growmighty.lectures.firstday.product.application.port.ProductIndexPort;
import com.growmighty.lectures.firstday.product.domain.Product;
import com.growmighty.lectures.firstday.product.infrastructure.ai.ProductEnrichService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ProductIndexPort의 Elasticsearch 구현체.
 * ★ Product → ProductDocument 변환은 인프라의 책임 — application은 도메인 모델만 다룬다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductIndexAdapter implements ProductIndexPort {

    private final ProductSearchRepository searchRepository;
    private final ProductEnrichService productEnrichService;

    @Override
    public void indexAll(List<Product> products) {
        long start = System.currentTimeMillis();

        List<ProductDocument> docs = new ArrayList<>();
        int done = 0;
        for (Product product : products) {
            var enriched = productEnrichService.enrich(product);
            docs.add(ProductDocument.from(product, enriched.attributes(), enriched.embedding()));
            if (++done % 10 == 0) {
                log.info("백필 진행 {}/{} ({}초 경과)", done, products.size(),
                    (System.currentTimeMillis() - start) / 1000);
            }
        }

        searchRepository.saveAll(docs);        // ★ 내부적으로 bulk API 사용 — Step 7에서 단건 저장과 비교
        log.info("백필 색인 완료: {}건, 총 {}초", docs.size(), (System.currentTimeMillis() - start) / 1000);
    }
}
