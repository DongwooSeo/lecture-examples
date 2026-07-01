package com.growmighty.lectures.firstday.tangledmonolith.order;

import com.growmighty.lectures.firstday.tangledmonolith.payment.Payment;
import com.growmighty.lectures.firstday.tangledmonolith.payment.PaymentService;
import com.growmighty.lectures.firstday.tangledmonolith.product.Product;
import com.growmighty.lectures.firstday.tangledmonolith.product.ProductService;
import com.growmighty.lectures.firstday.tangledmonolith.user.User;
import com.growmighty.lectures.firstday.tangledmonolith.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserService userService;
    private final ProductService productService;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    // 주문 생성.
    // 재고 확인하고 깎기, 단가*수량 합산, 총액 계산까지 전부 여기서 처리한다.
    // 쓰다 보니 OrderService 가 너무 많은 걸 알고 있고 금액 계산도 BigDecimal 로 여기저기 흩어져 있음.
    // 재고는 원래 Product 일, 총액은 Order 일인데 다 끌어와서 처리하는 중.
    @Transactional
    public OrderResult placeOrder(Long userId, List<OrderController.OrderItemRequest> requests) {
        // id로 user 찾기
        User user = userService.getUser(userId);

        // 이제 OrderService가 Item에 대한 책임을 내려놓는다.
        /*
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("주문할 상품이 없습니다.");
        }
        */

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderController.OrderItemRequest request : requests) {
            Product product = productService.getProduct(request.productId());
            orderItems.add(OrderItem.create(product.getName(), product.getPrice(), product.getId(), request.quantity()));
            product.decreaseStock(request.quantity());

            // 재고 확인하고 깎는 것, 모두 Product에게 책임을 넘긴다.
        }
        Order order = Order.create(user.getId(), orderItems);

        Payment payment = paymentService.pay(order.getTotalAmount().getValue());
        order.completePayment(payment.getId());

        Order savedOrder = orderRepository.save(order);
        return new OrderResult(savedOrder.getId());
    }

    public List<OrderResult> getOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(e -> new OrderResult(e.getId())).toList();
    }

    // 주문 항목 가격 변경.
    @Transactional
    public void changeItemPrice(Long orderId, Long orderItemId, BigDecimal newPrice) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. orderId=" + orderId));

        order.changeItemPrice(orderItemId, newPrice);
    }

    // 수량 변경.
    @Transactional
    public void changeItemQuantity(Long orderId, Long orderItemId, int newQuantity) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. orderId=" + orderId));

        order.changeItemQuantity(orderItemId, newQuantity);
    }

    // 저장된 총액이랑 항목으로 다시 계산한 총액을 비교해본다.
    @Transactional(readOnly = true)
    public OrderConsistencyView inspectOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다. orderId=" + orderId));

        Money storedTotal = order.getTotalAmount();

        Money recalculatedTotal = order.getItems().stream()
                .map(item -> item.getPrice().times(item.getQuantity()))
                .reduce(Money.zero(), Money::plus);

        boolean consistent = storedTotal.isSameAmount(recalculatedTotal);
        return new OrderConsistencyView(orderId, storedTotal.getValue(), recalculatedTotal.getValue(), consistent);
    }
}