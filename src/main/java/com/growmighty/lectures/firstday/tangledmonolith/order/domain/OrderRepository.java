package com.growmighty.lectures.firstday.tangledmonolith.order.domain;

import java.util.List;
import java.util.Optional;

public interface OrderRepository  {
    Order save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findAll();

    // 페이지 단위 조회하기 : 정산 데모 실험 할 때 조금씩 읽기에서 사용하려고 만듦.
    List<Order> findPage(int page, int size);

    long count();
}
