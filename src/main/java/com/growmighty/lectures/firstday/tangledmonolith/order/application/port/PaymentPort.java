package com.growmighty.lectures.firstday.tangledmonolith.order.application.port;

import com.growmighty.lectures.firstday.tangledmonolith.order.application.port.dto.PaymentResult;

import java.math.BigDecimal;

// API 호출 방법. Order는 PaymentService를 모른다. 대신 PaymentPort만 안다.
// API는 Facade와 달리 결합을 끊는 기술이다. DIP 구현.
public interface PaymentPort {
    PaymentResult pay(BigDecimal amount);

    void cancel(Long paymentId);
}
