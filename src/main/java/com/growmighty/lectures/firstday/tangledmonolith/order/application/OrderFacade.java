package com.growmighty.lectures.firstday.tangledmonolith.order.application;

import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderItemCommand;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderLine;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderResult;
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

import java.util.ArrayList;
import java.util.List;

/*
    - Facade 기법
    OrderService는 User와 Product와 Payment의 Service, 그리고 Order 생성과 저장까지 도맡았다.
    OrderService의 User에 대한 책임을 없애기 위해 Facade를 생성했다.
    placeOrder(주문 생성) Use Case는 주문 -> 상품 -> 결제 -> 회원 네 개 도메인이 모두 참여한다.
    그래서 이런 흐름을 Facade에게 맡긴다. 각 도메인은 자신의 할 일을 하고, 이 순서는 OrderFacade가 정해준다.
    쿠폰, 배송, 포인트 등이 생기면 기존 Service에서는 이 도메인까지 추가해야 하지만, Facade는 여전히 Order만 관리한다.
    하지만 결합은 그대로다. Facade는 결합을 없애는 것이 아니라 흐름을 정리하는 것이라고 볼 수 있다.
*/

@Service
@RequiredArgsConstructor
public class OrderFacade {
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final PaymentService paymentService;

    @Transactional
    public OrderResult placeOrder(OrderItemCommand command) {
        UserInfo user = userService.getUser(command.userId());

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
        Order order = Order.create(user.id(), orderItems);

        PaymentInfo payment = paymentService.pay(order.getTotalAmount().getValue());
        order.completePayment(payment.paymentId());

        return OrderResult.from(orderRepository.save(order));
    }
}
