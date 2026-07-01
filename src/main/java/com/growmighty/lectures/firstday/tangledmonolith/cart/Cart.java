package com.growmighty.lectures.firstday.tangledmonolith.cart;

import com.growmighty.lectures.firstday.tangledmonolith.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    // User 객체 전체를 가져오는 것이 아니라 id로만 접근한다.
    @Column(nullable = false)
    private Long userId;

    // 받아올 때도 userId를 Long으로 받아오자.
    public static Cart create(Long userId) {
        Cart cart = new Cart();
        cart.userId = userId;
        return cart;
    }

    public void addItem(CartItem item) {
        this.items.add(item);

        if (item.getCart() != this) {
            item.assignCart(this);
        }
    }

    public void clear() {
        this.items.clear();
    }
}
