package com.growmighty.lectures.firstday.product.presentation;

import com.growmighty.lectures.firstday.common.response.ApiResponse;
import com.growmighty.lectures.firstday.product.application.ProductSearchService;
import com.growmighty.lectures.firstday.product.application.ProductSearchSyncService;
import com.growmighty.lectures.firstday.product.application.dto.ProductSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products/search")
public class ProductSearchController {

    private final ProductSearchService searchService;
    private final ProductSearchSyncService syncService;

    @GetMapping
    public ApiResponse<List<ProductSearchResult>> search(
        @RequestParam String keyword,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(searchService.search(keyword, minPrice, maxPrice, page, size));
    }

    @GetMapping("/autocomplete")
    public ApiResponse<List<String>> autocomplete(@RequestParam String prefix) {
        return ApiResponse.ok(searchService.autocomplete(prefix));
    }

    @PostMapping("/internal/reindex")
    public ApiResponse<Long> reindex() {
        return ApiResponse.ok(syncService.reindexAll());
    }

    // presentation/ProductSearchController.java에 추가 — 응답도 DTO(ProductSearchResult)로, 인프라 모델 노출 금지
    @GetMapping("/semantic")
    public ApiResponse<List<ProductSearchResult>> semantic(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(searchService.semanticSearch(keyword, size));
    }

    @GetMapping("/hybrid")
    public ApiResponse<List<ProductSearchResult>> hybrid(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(searchService.hybridSearch(keyword, size));
    }
}
