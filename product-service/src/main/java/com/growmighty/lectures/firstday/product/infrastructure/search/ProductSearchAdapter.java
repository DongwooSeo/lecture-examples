package com.growmighty.lectures.firstday.product.infrastructure.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.FieldValueFactorModifier;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.growmighty.lectures.firstday.product.application.dto.ProductSearchResult;
import com.growmighty.lectures.firstday.product.application.port.ProductSearchPort;
import com.growmighty.lectures.firstday.product.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductSearchPort의 Elasticsearch 구현체.
 * ★ 쿼리 DSL(NativeQuery)과 ProductDocument는 여기서만 사용한다 — application으로 새어나가지 않게
 */
@Component
@RequiredArgsConstructor
public class ProductSearchAdapter implements ProductSearchPort {

    private final ElasticsearchOperations operations;
    private final ElasticsearchClient esClient;          // ★ 3-3 RRF용 저수준 클라이언트 — 미리 필드로
    private final EmbeddingModel embeddingModel;


    @Override
    public List<ProductSearchResult> search(String keyword, Double minPrice, Double maxPrice, int page, int size) {
        // ★ 3-2: ES 시간의 function_score 쿼리를 buildKeywordQuery로 추출 — 하이브리드의 키워드 축과 공유 (동작 동일)
        NativeQuery query = NativeQuery.builder()
            .withQuery(buildKeywordQuery(keyword, minPrice, maxPrice))
            .withPageable(PageRequest.of(page, size))
            .build();

        SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);
        return hits.getSearchHits().stream()
            .map(h -> toResult(h.getContent()))
            .toList();
    }

    @Override
    public List<String> autocomplete(String prefix) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(q -> q.match(m -> m.field("name.auto").query(prefix)))
            .withPageable(PageRequest.of(0, 10))
            .build();
        return operations.search(query, ProductDocument.class)
            .getSearchHits().stream()
            .map(h -> h.getContent().getName())
            .distinct()
            .toList();
    }

    @Override
    public List<ProductSearchResult> semanticSearch(String keyword, int size) {
        float[] queryVector = embeddingModel.embed(keyword);   // ① 검색어 임베딩 (색인과 같은 모델! 오전 귀결 ②)

        NativeQuery query = NativeQuery.builder()
            .withKnnSearches(knn -> knn                        // ② kNN (오전 4-4)
                .field("embedding")
                .queryVector(toFloatList(queryVector))
                .k(size)
                .numCandidates(100)
                .filter(f -> f.term(t -> t.field("status").value("ON_SALE"))))
            .withPageable(PageRequest.of(0, size))
            .build();

        return operations.search(query, ProductDocument.class)
            .getSearchHits().stream()
            .map(SearchHit::getContent)
            .map(this::toResult)                               // ③ Document → DTO — 인프라 모델은 여기서 멈춥니다
            .toList();
    }

    private static List<Float> toFloatList(float[] arr) {
        List<Float> list = new ArrayList<>(arr.length);
        for (float v : arr) list.add(v);
        return list;
    }

    private ProductSearchResult toResult(ProductDocument doc) {
        return new ProductSearchResult(
            doc.getId(),
            doc.getSellerId(),
            doc.getName(),
            doc.getDescription(),
            doc.getPrice(),
            ProductStatus.valueOf(doc.getStatus()),
            doc.getSalesCount(),
            doc.getOccasions(),
            doc.getStyles(),
            doc.getSeasons(),
            doc.getMaterial(),
            doc.isRainFriendly());
    }

    // infrastructure/search/ProductSearchAdapter.java — hybridSearch 구현 추가
    @Override
    public List<ProductSearchResult> hybridSearch(String keyword, int size) {
        try {
            List<Float> queryVector = toFloatList(embeddingModel.embed(keyword));

            SearchResponse<ProductDocument> res = esClient.search(s -> s
                    .index("products")
                    .size(size)
                    .retriever(r -> r.rrf(rrf -> rrf                              // ★ 오전 5-5 설계도 그대로
                        .retrievers(e -> e.retriever(rt -> rt.standard(st -> st
                            .query(buildKeywordQuery(keyword, null, null)))))     // 축 1: 키워드
                        .retrievers(e -> e.retriever(rt -> rt.knn(k -> k          // 축 2: 벡터
                            .field("embedding")
                            .queryVector(queryVector)
                            .k(50)
                            .numCandidates(200))))
                        .rankConstant(60)                                         // ★ 9.x부터 Integer (60L 아님)
                        .rankWindowSize(50))),
                ProductDocument.class);

            return res.hits().hits().stream().map(Hit::source).map(this::toResult).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // infrastructure/search/ProductSearchAdapter.java
    /** ES 시간에 만든 그 쿼리 그대로 — 하이브리드의 키워드 축으로 그대로 사용 (오전 5-5) */
    private static co.elastic.clients.elasticsearch._types.query_dsl.Query buildKeywordQuery(
        String keyword, Double minPrice, Double maxPrice) {
        return co.elastic.clients.elasticsearch._types.query_dsl.Query.of(q -> q.functionScore(fs -> fs
            .query(inner -> inner.bool(b -> {
                b.must(m -> m.multiMatch(mm -> mm
                    .query(keyword)
                    .fields("name^3", "description")        // 제목 3배 가중 + 동의어·nori는 매핑이 알아서
                    .fuzziness("AUTO")));                   // 오타 교정도 그대로
                b.filter(f -> f.term(t -> t.field("status").value("ON_SALE")));
                if (minPrice != null || maxPrice != null) {
                    b.filter(f -> f.range(r -> r.number(n -> {
                        n.field("price");
                        if (minPrice != null) n.gte(minPrice);
                        if (maxPrice != null) n.lte(maxPrice);
                        return n;
                    })));
                }
                return b;
            }))
            .functions(fn -> fn.fieldValueFactor(fv -> fv
                .field("salesCount").modifier(FieldValueFactorModifier.Log1p)
                .factor(2.0).missing(0.0)))                 // 판매량 랭킹도 그대로
            .boostMode(FunctionBoostMode.Sum)));
    }

}
