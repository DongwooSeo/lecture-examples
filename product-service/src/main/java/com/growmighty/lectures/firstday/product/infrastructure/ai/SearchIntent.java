package com.growmighty.lectures.firstday.product.infrastructure.ai;

import java.util.List;

// Step 4 (옵션·심화) — 검색어 의도 확장용 구조화 모델
public record SearchIntent(
    String expandedQuery,        // "꾸안꾸 데일리룩" → "자연스럽고 편안한 데일리 캐주얼"
    List<String> occasions,      // 필터로 쓸 TPO: ["데일리"]
    Boolean rainFriendly         // "비 오는 날" 감지 시 true, 아니면 null
) {}
