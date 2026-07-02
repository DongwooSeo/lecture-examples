package com.growmighty.lectures.firstday.tangledmonolith.order.infrastructure;

// 인프라 -> 도메인 (DIP)
import com.growmighty.lectures.firstday.tangledmonolith.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
}
