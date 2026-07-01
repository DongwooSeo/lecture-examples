package com.growmighty.lectures.firstday.tangledmonolith.seller;

import com.growmighty.lectures.firstday.tangledmonolith.product.Product;
import com.growmighty.lectures.firstday.tangledmonolith.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter // get 추가
@Table(name = "sellers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 판매자와 상품은 서로 다른 애그리게잇이니 지우자.
    /*
    @OneToMany(mappedBy = "seller")
    private List<Product> products = new ArrayList<>();
    */

    // User를 id로만 접근
    @Column(nullable = false)
    private Long userId;

    public static Seller create(Long userId) {
        Seller seller = new Seller();
        seller.userId = userId;
        
        return seller;
    }
}
