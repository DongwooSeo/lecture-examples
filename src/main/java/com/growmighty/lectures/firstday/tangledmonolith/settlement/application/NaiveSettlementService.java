package com.growmighty.lectures.firstday.tangledmonolith.settlement.application;

/*
    실험용. 배치의 필요성에 대해 테스트하는 서비스 파일이다.
    실무 운영에서는 절대 쓰지 않는다.
    첫 번째는 @code findAll()로 전량을 한 번에 메모리로 올린다. OutOfMemoryError 예외가 발생할 것이다.
    두 번째는 페이지 단위로 조금씩 읽되 전부 메모리에 쌓으며 정산한다.
    limit을 올릴수록 메모리와 시간이 어떻게 증가하는지 확인해보자. 점점 OOM 예외에 가까워질 것이다.

 */

import com.growmighty.lectures.firstday.tangledmonolith.order.domain.Order;
import com.growmighty.lectures.firstday.tangledmonolith.order.domain.OrderRepository;
import com.growmighty.lectures.firstday.tangledmonolith.order.domain.OrderStatus;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.application.dto.SettleReport;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.domain.Settlement;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.domain.SettlementRepository;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.support.HeapMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaiveSettlementService {

    /** 플랫폼 수수료율 3% */
    private static final BigDecimal FEE_RATE = new BigDecimal("0.03");

    /** 한 번에 읽어오는 페이지 크기 */
    private static final int PAGE_SIZE = 10_000;

    private final OrderRepository orderRepository;
    private final SettlementRepository settlementRepository;

    /**
     * [데모1] "한 줄의 함정" — 전량을 메모리로 적재(findAll)한 뒤 정산.
     * 100만 건이면 findAll 그 한 줄에서 OOM 이 발생한다.
     */
    @Transactional
    public SettleReport settleAll() {
        try (HeapMonitor monitor = HeapMonitor.start("naive-findAll", 500)) {
            long startedAt = System.currentTimeMillis();
            log.warn("[NAIVE] findAll() 시작 — 전체 주문을 한 번에 메모리로 올립니다. (대용량이면 여기서 OOM)");

            // ⚠️ 바로 이 한 줄이 100만 엔티티를 통째로 힙에 올린다.
            List<Order> orders = orderRepository.findAll();
            log.warn("[NAIVE] findAll() 완료 — 적재된 주문 수 = {}", orders.size());

            long settled = settleEach(orders);
            long elapsed = System.currentTimeMillis() - startedAt;

            SettleReport report = new SettleReport(
                orders.size(), settled, elapsed, monitor.peakUsedMb(), monitor.maxHeapMb());
            log.warn("[NAIVE] 완료 리포트 = {}", report);
            return report;
        }
    }

    /**
     * [데모2] "절벽으로 걸어가기" — 페이지로 조금씩 읽되 전부 메모리에 누적하며 정산.
     * limit 을 올려갈수록 메모리/시간이 어떻게 늘어나는지 추세를 보여준다.
     *
     * @param limit 정산할 최대 주문 수 (전체를 보려면 매우 크게)
     */
    @Transactional
    public SettleReport settleUpTo(int limit) {
        try (HeapMonitor monitor = HeapMonitor.start("naive-climb", 500)) {
            long startedAt = System.currentTimeMillis();
            log.warn("[NAIVE] '절벽으로 걸어가기' 시작 — limit={} 까지 메모리에 쌓으며 정산", limit);

            // 안티패턴 재현: 읽은 주문을 절대 버리지 않고 계속 보관한다.
            List<Order> holding = new ArrayList<>();
            long settled = 0;
            int page = 0;

            while (holding.size() < limit) {
                List<Order> batch = orderRepository.findPage(page++, PAGE_SIZE);
                if (batch.isEmpty()) {
                    break;
                }
                holding.addAll(batch);
                settled += settleEach(batch);

                long elapsed = System.currentTimeMillis() - startedAt;
                log.warn("[NAIVE] 누적 {}건 보관 / 정산 {}건 / 경과 {}ms (메모리는 위 [mem] 로그 참고)",
                    holding.size(), settled, elapsed);
            }

            long elapsed = System.currentTimeMillis() - startedAt;
            SettleReport report = new SettleReport(
                holding.size(), settled, elapsed, monitor.peakUsedMb(), monitor.maxHeapMb());
            log.warn("[NAIVE] 완료 리포트 = {}", report);
            return report;
        }
    }

    private long settleEach(List<Order> orders) {
        long settled = 0;
        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.PAID || order.getPaymentId() == null) {
                continue;
            }
            BigDecimal amount = order.getTotalAmount().getValue();
            settlementRepository.save(Settlement.of(order.getId(), order.getPaymentId(), amount, FEE_RATE));
            settled++;
        }
        return settled;
    }
}
