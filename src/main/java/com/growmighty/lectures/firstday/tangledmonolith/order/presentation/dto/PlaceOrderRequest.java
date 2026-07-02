package com.growmighty.lectures.firstday.tangledmonolith.order.presentation.dto;

import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderItemCommand;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.dto.OrderLine;
import lombok.NonNull;

import java.util.List;

public record PlaceOrderRequest(
        @NonNull Long userId,
        @NonNull List<OrderItemRequest> requests
) {
    public record OrderItemRequest(@NonNull Long productId, @NonNull Integer quantity) {
    }
    public OrderItemCommand toCommand() {
        List<OrderLine> lines =  requests.stream()
                .map(r -> new OrderLine(r.productId(), r.quantity()))
                .toList();

        return new OrderItemCommand(userId, lines);
    }
}
