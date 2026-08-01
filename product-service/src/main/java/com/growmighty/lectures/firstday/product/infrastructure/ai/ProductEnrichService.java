package com.growmighty.lectures.firstday.product.infrastructure.ai;

import com.growmighty.lectures.firstday.product.domain.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductEnrichService {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;

    public ProductEnrichService(ChatClient.Builder builder, EmbeddingModel embeddingModel) {
        this.chatClient = builder
            .defaultSystem("""
                당신은 패션 상품 데이터를 분석하는 어시스턴트입니다.
                상품명과 설명에 명시된 정보만 사용하고, 절대 추측하지 마세요.
                해당 정보가 없으면 빈 배열 또는 false를 반환하세요.
                """)                                  // ★ 환각 방어 1차 (오전 3-5 ⚠️, 부록 Q3)
            .build();
        this.embeddingModel = embeddingModel;
    }

    /** 색인에 필요한 AI 산출물 묶음 */
    public record Enriched(ProductAttributes attributes, float[] embedding) {}

    public Enriched enrich(Product product) {
        ProductAttributes attrs = safeExtract(product);       // ① 작가 — 실패 가능
        float[] embedding = safeEmbed(product, attrs);        // ② 사서 — 실패 가능
        return new Enriched(attrs, embedding);
    }

    private ProductAttributes safeExtract(Product p) {
        try {
            return chatClient.prompt()
                .user("""
                    다음 패션 상품에서 속성을 한글로 추출해줘.
                    상품명: %s
                    설명: %s
                    """.formatted(p.getName(), p.getDescription()))
                .call()
                .entity(ProductAttributes.class);             // ★ Structured Output (오전 3-5)
        } catch (Exception e) {
            log.warn("속성 추출 실패 productId={} — 속성 없이 색인, 백필로 보정", p.getId(), e);
            return ProductAttributes.empty();                 // ★ 부분 실패 정책 (아래 토론)
        }
    }

    private float[] safeEmbed(Product p, ProductAttributes attrs) {
        try {
            return embeddingModel.embed(buildEmbeddingText(p, attrs));
        } catch (Exception e) {
            log.warn("임베딩 실패 productId={} — 벡터 없이 색인, 백필로 보정", p.getId(), e);
            return null;                                      // 벡터 없는 문서도 키워드 검색은 됨
        }
    }

    // Step 4 (옵션·심화) — 검색어 의도 분석. 검색마다 챗 호출 = 지연 +2~5초, 대가를 계산하고 쓸 것
    public SearchIntent analyzeQuery(String keyword) {
        return chatClient.prompt()
            .user("""
                다음 패션 검색어의 의도를 분석해줘.
                expandedQuery에는 검색 의도를 풀어쓴 문장을,
                occasions에는 감지된 TPO를, rainFriendly는 우천 관련일 때만 true를.
                검색어: %s
                """.formatted(keyword))
            .call()
            .entity(SearchIntent.class);
    }

    /** 무엇을 임베딩할 것인가 — 이름+설명+속성 (오전 4-5 '빈약한 벡터' 방어) */
    private String buildEmbeddingText(Product p, ProductAttributes a) {
        return "%s. %s. 상황: %s. 스타일: %s. 계절: %s. 소재: %s.%s".formatted(
            p.getName(), p.getDescription(),
            String.join(", ", a.occasions()),
            String.join(", ", a.styles()),
            String.join(", ", a.seasons()),
            a.material() == null ? "" : a.material(),
            a.rainFriendly() ? " 비 오는 날 착용 적합." : "");
    }
}
