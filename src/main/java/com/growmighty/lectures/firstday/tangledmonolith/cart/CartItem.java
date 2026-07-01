package com.growmighty.lectures.firstday.tangledmonolith.cart;


import com.growmighty.lectures.firstday.tangledmonolith.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    // 역시 Product 자체가 아닌 id로만 접근한다.
    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    public static CartItem create(Long productId, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.productId = productId;
        cartItem.quantity = quantity;
        return cartItem;
    }

    void assignCart(Cart cart) {
        this.cart = cart;
    }
}
