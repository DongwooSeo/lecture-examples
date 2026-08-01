package com.growmighty.lectures.firstday.product.application;

import com.growmighty.lectures.firstday.product.application.dto.ProductSearchResult;
import com.growmighty.lectures.firstday.product.application.port.ProductSearchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchPort searchPort;   // ★ 인프라(Elasticsearch)가 아닌 포트에 의존 (DIP)

    public List<ProductSearchResult> search(String keyword, Double minPrice, Double maxPrice, int page, int size) {
        return searchPort.search(keyword, minPrice, maxPrice, page, size);
    }

    public List<String> autocomplete(String prefix) {
        return searchPort.autocomplete(prefix);
    }

    public List<ProductSearchResult> semanticSearch(String keyword, int size) {
        return searchPort.semanticSearch(keyword, size);
    }

    public List<ProductSearchResult> hybridSearch(String keyword, int size) {
        return searchPort.hybridSearch(keyword, size);
    }
}
