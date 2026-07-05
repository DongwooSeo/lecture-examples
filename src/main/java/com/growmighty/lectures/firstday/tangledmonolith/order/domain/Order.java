package com.growmighty.lectures.firstday.tangledmonolith.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    private static final Money FREE_SHIPPING_THRESHOLD = Money.from(BigDecimal.valueOf(50_000));
    private static final Money BASE_SHIPPING_FEE = Money.from(BigDecimal.valueOf((3_000)));

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User를 id로만 접근
    @Column(nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    private List<OrderItem> items = new ArrayList<>();

    // Payment를 id로만 접근
    @Column
    private Long paymentId;
    
    @Embedded // VO를 적용한다.
    // Money.value를 total_amount 컬럼에 매핑해준다.
    @AttributeOverride(
            name = "value",
            column = @Column(name = "total_amount", nullable = false)
    )
    private Money totalAmount; // BigDecimal에서 Money로 바꿔준다.

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "items_amount", nullable = false))
    private Money itemsAmount;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "shipping_fee", nullable = false))
    private Money shippingFee;

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
        recalculateAmounts();
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

    private void recalculateAmounts() {
        this.itemsAmount = items.stream()
                .map(OrderItem::subtotal)
                .reduce(Money.zero(), Money::plus);
        this.shippingFee = calculateShippingFee(this.itemsAmount);
        this.totalAmount = this.itemsAmount.plus(this.shippingFee);
    }

    private Money calculateShippingFee(Money itemsAmount) {
        return itemsAmount.isGreaterThanOrEqual(FREE_SHIPPING_THRESHOLD)
                ? Money.zero()
                : BASE_SHIPPING_FEE;
    }

    public Money recalculatedTotal() {
        Money items = this.items.stream()
                .map(OrderItem::subtotal)
                .reduce(Money.zero(), Money::plus);
        return items.plus(calculateShippingFee(items));
    }

    // 상태 변경을 의미 있는 메서드로 제한했다.
    public void completePayment(Long paymentId) {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("결제 가능한 상태가 아닙니다. 현재 상태: " + this.status);
        }
        this.status = OrderStatus.PAID;
        this.paymentId = paymentId;
    }

    public void cancel() {
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    // 다음 두 메서드는 변경 사항 감지 시 총액을 재계산하여 정합성을 유지한다.
    public void changeItemPrice(Long orderItemId, BigDecimal newPrice) {
        findItem(orderItemId).changePrice(newPrice);
        recalculateAmounts();
    }

    public void changeItemQuantity(Long orderItemId, int newQuantity) {
        findItem(orderItemId).changeQuantity(newQuantity);
        recalculateAmounts();
    }

    private OrderItem findItem(Long orderItemId) {
        return items.stream()
                .filter(e -> e.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("주문에 없는 항목입니다."));
    }
}
