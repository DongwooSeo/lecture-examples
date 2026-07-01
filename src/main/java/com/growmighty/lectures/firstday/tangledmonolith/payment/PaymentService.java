package com.growmighty.lectures.firstday.tangledmonolith.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

// Repo와 소통하는 기능은 Service 계층으로
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public Payment pay(BigDecimal totalAmount) {
        Payment payment = Payment.ready(totalAmount);
        payment.changeStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);

        return payment;
    }
}
