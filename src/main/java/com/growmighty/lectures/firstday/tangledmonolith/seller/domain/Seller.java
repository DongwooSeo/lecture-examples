package com.growmighty.lectures.firstday.tangledmonolith.seller.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter // get 추가
@Table(name = "sellers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 판매자와 상품은 서로 다른 애그리게잇이니 지우자.

    // User를 id로만 접근
    @Column(nullable = false)
    private Long userId;

    // 두 가지의 컬럼 추가
    @Column(nullable = false)
    private String businessName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerStatus status;

    // 생성자
    private Seller(Long userId, String businessName) {
        if (businessName == null || businessName.isBlank()) {
            throw new IllegalArgumentException("상호명은 필수입니다.");
        }
        this.userId = userId;
        this.businessName = businessName;
        this.status = SellerStatus.ACTIVE;
    }

    // 판매자로 등록 요청
    public static Seller apply(Long userId, String businessName) {
        return new Seller(userId, businessName);
    }

    // 상태 변경, Seller 자신의 상태를 변경하는 것이니 Seller 책임!
    public void suspend() {
        this.status = SellerStatus.SUSPENDED;
    }

    public void activate() {
        this.status = SellerStatus.ACTIVE;
    }

    public boolean canSell() {
        return this.status == SellerStatus.ACTIVE;
    }
}
