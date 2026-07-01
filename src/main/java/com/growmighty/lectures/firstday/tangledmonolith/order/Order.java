package com.growmighty.lectures.firstday.tangledmonolith.order;

import com.growmighty.lectures.firstday.tangledmonolith.payment.Payment;
import com.growmighty.lectures.firstday.tangledmonolith.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User를 id로만 접근
    @Column(nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    private List<OrderItem> items = new ArrayList<>();

    // Payment를 id로만 접근
    @Column(nullable = false)
    private Long paymentId;
    
    @Embedded // VO를 적용한다.
    // Money.value를 total_amount 컬럼에 매핑해준다.
    @AttributeOverride(
            name = "value",
            column = @Column(name = "total_amount", nullable = false)
    )
    private Money totalAmount; // BigDecimal에서 Money로 바꿔준다.

    // @Setter를 제거하여 status를 외부에서 아무렇게나 바꾸지 못하게 한다.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private Order(Long userId, List<OrderItem> items) {
        validateItems(items); // 생성 규칙을 캡슐화했다.
        items.forEach(this::addOrderItem);
        this.userId = userId;
        this.status = OrderStatus.CREATED;
        // 총액 계산 책임을 Service가 아닌 Order가 책임진다.
        this.totalAmount = calculateTotalAmount(items);
    }

    public static Order create(Long userId, List<OrderItem> items) {
        return new Order(userId, items);
    }

    private void validateItems(List<OrderItem> items)
    {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("주문할 상품이 없습니다.");
        }
    }

    private void addOrderItem(OrderItem item) {
        this.items.add(item);

        if (item.getOrder() != this) {
            item.assignOrder(this);
        }
    }

    private Money calculateTotalAmount(List<OrderItem> items) {
        return items.stream()
                .map(e -> e.getPrice().times(e.getQuantity()))
                .reduce(Money.zero(), Money::plus);
    }

    // 상태 변경을 의미 있는 메서드로 제한했다.
    public void completePayment(Long paymentId) {
        this.status = OrderStatus.PAID;
        this.paymentId = paymentId;
    }

    // 다음 두 메서드는 변경 사항 감지 시 총액을 재계산하여 정합성을 유지한다.
    public void changeItemPrice(Long orderItemId, BigDecimal newPrice) {
        OrderItem target = items.stream()
                .filter(e -> e.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("주문에 없는 항목입니다."));

        target.changePrice(newPrice);
        this.totalAmount = calculateTotalAmount(items);
    }

    public void changeItemQuantity(Long orderItemId, int newQuantity) {
        OrderItem target = items.stream()
                .filter(e -> e.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("주문에 없는 항목입니다."));

        target.changeQuantity(newQuantity);
        this.totalAmount = calculateTotalAmount(items);
    }
}
