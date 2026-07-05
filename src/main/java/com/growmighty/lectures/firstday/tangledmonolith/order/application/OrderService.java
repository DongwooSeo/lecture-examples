package com.growmighty.lectures.firstday.tangledmonolith.order.application;

import com.growmighty.lectures.firstday.tangledmonolith.cart.application.CartService;
import com.growmighty.lectures.firstday.tangledmonolith.cart.application.dto.CartView;
import com.growmighty.lectures.firstday.tangledmonolith.common.exception.EntityNotFoundException;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderConsistencyView;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderItemCommand;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderLine;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderResult;
import com.growmighty.lectures.firstday.tangledmonolith.order.domain.Money;
import com.growmighty.lectures.firstday.tangledmonolith.order.domain.Order;
import com.growmighty.lectures.firstday.tangledmonolith.order.domain.OrderItem;
import com.growmighty.lectures.firstday.tangledmonolith.order.domain.OrderRepository;
import com.growmighty.lectures.firstday.tangledmonolith.payment.application.PaymentService;
import com.growmighty.lectures.firstday.tangledmonolith.payment.application.dto.PaymentInfo;
import com.growmighty.lectures.firstday.tangledmonolith.product.application.ProductService;
import com.growmighty.lectures.firstday.tangledmonolith.product.application.dto.ProductInfo;
import com.growmighty.lectures.firstday.tangledmonolith.user.application.UserService;
import com.growmighty.lectures.firstday.tangledmonolith.user.application.dto.UserInfo;
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
    private final CartService cartService;

    // 주문 생성.
    // 재고 확인하고 깎기, 단가*수량 합산, 총액 계산까지 전부 여기서 처리한다.
    // 쓰다 보니 OrderService 가 너무 많은 걸 알고 있고 금액 계산도 BigDecimal 로 여기저기 흩어져 있음.
    // 재고는 원래 Product 일, 총액은 Order 일인데 다 끌어와서 처리하는 중.
    @Transactional
    // 이제 PlaceOrderCommand라는 DTO를 이용하여 정형화된 요청과 응답을 구성한다.
    public OrderResult placeOrder(OrderItemCommand command) {
        // id로 user 찾기
        UserInfo userInfo = userService.getUser(command.userId());

        List<OrderLine> lines = command.lines();
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("주문할 상품이 없습니다.");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderLine line : lines) {
            ProductInfo product = productService.getProductInfo(line.productId());
            orderItems.add(OrderItem.create(product.name(), product.price(), product.id(), line.quantity()));
            productService.decreaseStock(line.productId(), line.quantity());
        }
        Order order = Order.create(userInfo.id(), orderItems);

        PaymentInfo payment = paymentService.pay(order.getTotalAmount().getValue());
        order.completePayment(payment.paymentId());

        return OrderResult.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResult placeOrderFromCart(Long userId) {
        CartView cart = cartService.getCart(userId);
        if (cart.items().isEmpty()) {
            throw new IllegalStateException("장바구니가 비어 있습니다. userId= " + userId);
        }
        List<OrderLine> lines = cart.items().stream()
                .map(line -> new OrderLine(line.productId(), line.quantity()))
                .toList();

        OrderResult result = placeOrder(new OrderItemCommand(userId, lines));
        cartService.clear(userId);
        return result;
    }

    @Transactional
    public OrderResult cancelOrder(Long orderId) {
        Order order = getOrder(orderId);

        order.cancel();

        for (OrderItem item : order.getItems()) {
            productService.restoreStock(item.getProductId(), item.getQuantity());
        }
        if (order.getPaymentId() != null) {
            paymentService.cancel(order.getPaymentId());
        }
        return OrderResult.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResult> getOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResult::from)
                .toList();
    }

    // 주문 항목 가격 변경.
    @Transactional
    public void changeItemPrice(Long orderId, Long orderItemId, BigDecimal newPrice) {
        getOrder(orderId).changeItemPrice(orderItemId, newPrice);
    }

    // 수량 변경.
    @Transactional
    public void changeItemQuantity(Long orderId, Long orderItemId, int newQuantity) {
        getOrder(orderId).changeItemQuantity(orderItemId, newQuantity);
    }

    // 저장된 총액이랑 항목으로 다시 계산한 총액을 비교해본다.
    @Transactional(readOnly = true)
    public OrderConsistencyView inspectOrder(Long orderId) {
        Order order = getOrder(orderId);
        Money storedTotal = order.getTotalAmount();
        Money recalculatedTotal = order.recalculatedTotal();

        return new OrderConsistencyView(
                orderId,
                storedTotal.getValue(),
                recalculatedTotal.getValue(),
                storedTotal.isSameAmount(recalculatedTotal));
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 주문입니다. orderId= " + orderId));
    }
}