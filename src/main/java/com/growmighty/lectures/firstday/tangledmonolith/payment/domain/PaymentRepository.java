package com.growmighty.lectures.firstday.tangledmonolith.payment.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// DIP 구현!
public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(Long id);
}
