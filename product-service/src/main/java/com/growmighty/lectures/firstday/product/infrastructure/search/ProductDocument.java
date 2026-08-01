package com.growmighty.lectures.firstday.product.infrastructure.search;

import com.growmighty.lectures.firstday.product.domain.Product;
import com.growmighty.lectures.firstday.product.infrastructure.ai.ProductAttributes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/product-settings.json")   // Step 2에서 만들 파일
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    @Id
    private Long id;                       // ★ productId를 _id로 → 색인이 자연스럽게 "멱등 upsert"가 된다 (오전 §6-4 원칙 ①)

    @Field(type = FieldType.Long)
    private Long sellerId;

    @MultiField(
        mainField = @Field(type = FieldType.Text,
            analyzer = "korean_index",
            searchAnalyzer = "korean_search"),   // ★ 동의어는 검색 시점에만! (오전 §5-1)
        otherFields = {
            @InnerField(suffix = "auto", type = FieldType.Text,
                analyzer = "autocomplete_index",         // ★ edge_ngram은 색인 시점에만! (오전 §5-2)
                searchAnalyzer = "korean_index")
        })
    private String name;

    @Field(type = FieldType.Text, analyzer = "korean_index", searchAnalyzer = "korean_search")
    private String description;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Keyword)      // ★ 필터용 → keyword. text로 하면 오전 §3-4의 참사
    private String status;

    @Field(type = FieldType.Long)         // ★ 랭킹 시그널
    private long salesCount;

    // infrastructure/search/ProductDocument.java — 기존 필드 아래에 추가
    @Field(type = FieldType.Keyword)      // ★ 필터·집계용 → keyword (오전 3-5 "걸러낼 값이면 keyword")
    private List<String> occasions;

    @Field(type = FieldType.Keyword)
    private List<String> styles;

    @Field(type = FieldType.Keyword)
    private List<String> seasons;

    @Field(type = FieldType.Keyword)
    private String material;

    @Field(type = FieldType.Boolean)
    private boolean rainFriendly;

    @Field(type = FieldType.Dense_Vector, dims = 1024)   // ★ bge-m3 = 1,024차원. 모델 바꾸면? 재색인! (오전 귀결 ③)
    private float[] embedding;

    /**
     * 기본 상품 정보만으로 Document 생성 (AI 데이터가 없는 경우)
     */
    public static ProductDocument from(Product p) {
        return new ProductDocument(
            p.getId(),
            p.getSellerId(),
            p.getName(),
            p.getDescription(),
            p.getPrice(),
            p.getStatus().name(),
            p.getSalesCount(),
            null,   // occasions
            null,   // styles
            null,   // seasons
            null,   // material
            false,  // rainFriendly
            null    // embedding
        );
    }

    /**
     * AI 추출 속성 및 벡터 임베딩을 포함하여 Document 생성
     */
    public static ProductDocument from(Product p, ProductAttributes a, float[] embedding) {
        ProductDocument doc = from(p);          // 기존 기본 필드 매핑 로직 재사용
        if (a != null) {
            doc.occasions = a.occasions();
            doc.styles = a.styles();
            doc.seasons = a.seasons();
            doc.material = a.material();
            doc.rainFriendly = a.rainFriendly();
        }
        doc.embedding = embedding;              // null 허용 — 벡터 없는 문서 (부분 실패)
        return doc;
    }
}
