package com.growmighty.lectures.firstday.product.infrastructure.ai;

import java.util.List;

public record ProductAttributes(
    List<String> occasions,      // TPO: ["결혼식 하객", "오피스", "데이트"]
    List<String> styles,         // ["세미 포멀", "미니멀"]
    List<String> seasons,        // ["봄", "가을"]
    String material,             // "새틴"
    boolean rainFriendly         // 우천 적합 (발수·방수 소재)
) {
    public static ProductAttributes empty() {
        return new ProductAttributes(List.of(), List.of(), List.of(), null, false);
    }
}
