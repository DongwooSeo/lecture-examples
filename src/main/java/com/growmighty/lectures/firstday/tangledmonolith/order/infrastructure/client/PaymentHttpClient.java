package com.growmighty.lectures.firstday.tangledmonolith.order.infrastructure.client;

import com.growmighty.lectures.firstday.tangledmonolith.order.application.port.PaymentPort;
import com.growmighty.lectures.firstday.tangledmonolith.order.application.port.dto.PaymentResult;
import com.growmighty.lectures.firstday.tangledmonolith.order.infrastructure.client.dto.ApiResponseBody;
import com.growmighty.lectures.firstday.tangledmonolith.order.infrastructure.client.dto.PayBody;
import com.growmighty.lectures.firstday.tangledmonolith.order.infrastructure.client.dto.PaymentApiData;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

/*
    Postman으로 하는 걸 JAVA 코드로 적은 것임.

 */
@Component
@RequiredArgsConstructor
public class PaymentHttpClient implements PaymentPort {
    private final RestClient orderRestClient;

    @Override
    public PaymentResult pay(BigDecimal amount) {
        ApiResponseBody<PaymentApiData> body = orderRestClient.post() // POST를 보낸다.
                .uri("/payments") // POST /payment
                .contentType(MediaType.APPLICATION_JSON) // Content-Type: application/json
                .body(new PayBody(amount)) // JSON의 Body 구성. "amount":10000 이렇게 뜰 것이다.
                .retrieve() // 라는 요청을 실제로 보낸다.
                .body(new ParameterizedTypeReference<>() { // 응답을 JSON에서 Java 객체로 변환한다.
                });

        PaymentApiData data = body.data(); // 변환된 Java 객체를 꺼낸다.
        return new PaymentResult(data.paymentId(), data.amount(), data.status()); // 꺼낸 객체를 Order가 쓰는 객체로 변환한다.
    }

    @Override
    public void cancel(Long paymentId) {
        orderRestClient.post()
                .uri("/payments/{paymentId}/cancel", paymentId)
                .retrieve()
                .toBodilessEntity(); // 응답은 필요 없다.
    }
}
