package com.growmighty.lectures.firstday.tangledmonolith.order.application;

import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderItemCommand;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderLine;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderResult;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.port.PaymentPort;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.port.ProductPort;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.port.dto.PaymentResult;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.port.dto.ProductSnapshot;
import com.growmighty.lectures.firstday.tangledmonolith.order.domain.Order;
import com.growmighty.lectures.firstday.tangledmonolith.order.domain.OrderItem;
import com.growmighty.lectures.firstday.tangledmonolith.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/*법
    - API 기법
    Order 도메인이 Product/Payment 도메인의 내부 구현에 직접 의존하지 않게 만들었다.
    원래는 OrderService가 ProductService, PaymentService를 직접 맡고 있었다.
    따라서 상대 도메인의 DTO까지 import해야 했고, 영향 받는다.
    이제 Order는 다른 도메인의 Service는 모른다. 대신 Order가 필요로 하는 기능만 interface로 모아놓은 Port에 의존한다.
    DIP를 적용해 도메인 간 결합도를 낮추고 나중에 API 호출이나 MSA 분리로 확장하기 쉬운 구조로 리팩토링 되었다.
*/

@Service
@RequiredArgsConstructor
public class OrderApiService {
    private final OrderRepository orderRepository;
    private final ProductPort productPort;
    private final PaymentPort paymentPort;

    public OrderResult placeOrder(OrderItemCommand command) {
        List<OrderLine> lines = command.lines();
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("주문할 상품이 없습니다.");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderLine line : lines) {
            ProductSnapshot product = productPort.getProduct(line.productId());
            if (!product.orderable()) {
                throw new IllegalStateException("현재 구매할 수 없는 상품입니다. productId= " + product.productId());
            }
            orderItems.add(OrderItem.create(product.name(), product.price(), product.productId(), line.quantity()));
            productPort.decreaseStock(line.productId(), line.quantity());
        }
        Order order = Order.create(command.userId(), orderItems);

        PaymentResult payment = paymentPort.pay(order.getTotalAmount().getValue());
        order.completePayment(payment.paymentId());

        return OrderResult.from(orderRepository.save(order));
    }
}
