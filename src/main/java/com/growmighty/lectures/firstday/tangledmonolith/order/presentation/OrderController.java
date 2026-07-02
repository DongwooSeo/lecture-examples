package com.growmighty.lectures.firstday.tangledmonolith.order.presentation;

import com.growmighty.lectures.firstday.tangledmonolith.order.application.OrderService;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderConsistencyView;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderResult;

import com.growmighty.lectures.firstday.tangledmonolith.order.domain.OrderRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

// Controller는 presentation 계층으로.
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderResult> getOrders() {
        return orderService.getOrders();
    }

    @PostMapping
    public OrderResult placeOrder(@RequestBody OrderCreationRequest request) {
        return orderService.placeOrder(request.userId(), request.requests()); // OrderItemCommand.dto
    }

    // 표현 -> 응용
    @GetMapping("/{orderId}/inspect")
    public OrderConsistencyView inspectOrder(@PathVariable Long orderId) {
        return orderService.inspectOrder(orderId);
    }

    // 항목 가격만 바꾸는 API, 하지만 Order의 개입을 받아야만 한다.
    @PatchMapping("/orderItems/{orderItemId}/price")
    public void changeOrderItemPrice(@PathVariable Long orderId, @PathVariable Long orderItemId, @RequestParam BigDecimal price) {
        orderService.changeItemPrice(orderId, orderItemId, price);
    }

    // 수량만 바꾸는 API. 위와 똑같이 총액은 그대로 남는다.
    @PatchMapping("/orderItems/{orderItemId}/quantity")
    public void changeOrderItemQuantity(@PathVariable Long orderId, @PathVariable Long orderItemId, @RequestParam int quantity) {
        orderService.changeItemQuantity(orderId, orderItemId, quantity);
    }

    public record OrderCreationRequest(@NonNull Long userId, @NonNull List<OrderItemRequest> requests) {

    }

    public record OrderItemRequest(@NonNull Long productId, @NonNull Integer quantity) {

    }
}