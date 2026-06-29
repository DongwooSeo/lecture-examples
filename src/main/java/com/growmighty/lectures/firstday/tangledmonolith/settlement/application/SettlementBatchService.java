package com.growmighty.lectures.firstday.tangledmonolith.settlement.application;

import com.growmighty.lectures.firstday.tangledmonolith.order.domain.OrderRepository;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.application.dto.SettleReport;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.batch.SettlementFaultBox;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.domain.SettlementRepository;
import com.growmighty.lectures.firstday.tangledmonolith.settlement.support.HeapMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Service;

/**
 * 정산 배치 Job 실행기.
 *
 * <p>{@code spring.batch.job.enabled=false} 라 부팅 시 자동 실행되지 않으므로,
 * 이 서비스가 {@link JobOperator} 로 직접 실행한다.
 * (Batch 6 에서 JobLauncher 는 deprecated → JobOperator 사용)
 *
 * <p><b>[Step2]</b> {@link #run()} — 매번 새 JobParameters(timestamp)로 실행. {@link HeapMonitor}
 * 로 감싸 피크 힙이 chunk 크기만큼만 유지되는 걸 보여준다.
 *
 * <p><b>[Step3] 멱등성 vs 재시작 — 두 가지 다른 무기</b>
 * <ul>
 *   <li>{@link #runFailing(double)} + {@link #run()} : <b>애플리케이션 레벨 멱등성</b>.
 *       매 실행이 <b>새 JobInstance</b>(timestamp)다. 처음엔 일부러 50%에서 실패시키고,
 *       다시 {@link #run()} 하면 Processor 의 {@code existsByOrderId} 스킵이 이미 정산된 50%를
 *       건너뛰고 남은 50%만 처리한다. "몇 번을 다시 돌려도 한 주문은 한 번만."</li>
 *   <li>{@link #runRestartable(long, Double)} : <b>프레임워크 레벨 재시작</b>.
 *       <b>같은 JobParameters(runId)</b>로 다시 실행하면 Spring Batch 가 실패한 같은 JobInstance 를
 *       이어서 재개한다. Reader 의 체크포인트(ExecutionContext)가 마지막 커밋 지점을 기억해
 *       앞부분은 아예 다시 읽지 않는다.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementBatchService {

    private final JobOperator jobOperator;
    private final Job settlementJob;
    private final SettlementFaultBox faultBox;
    private final OrderRepository orderRepository;
    private final SettlementRepository settlementRepository;

    /**
     * [Step2] 정상 정산. 매번 새 JobParameters 로 실행한다.
     * (이미 정산된 건은 Processor 가 멱등 스킵하므로 몇 번을 돌려도 안전 = 멱등성 데모의 "복구 실행")
     */
    public SettleReport run() {
        faultBox.disarm(); // 혹시 직전에 장전돼 있었다면 해제 → 깨끗한 정상 실행
        JobParameters params = newRunParams();
        log.warn("[BATCH] 정산 Job 실행 (새 인스턴스)");
        return launch(params, "batch");
    }

    /**
     * [Step3-1] 50% 지점에서 강제로 터지는 실행. (장애 시나리오 재현)
     *
     * <p>아직 정산되지 않은 주문 수를 센 뒤 그 비율만큼 처리한 직후 {@code SettlementFaultException}
     * 을 던지게 장전한다. 직전까지 커밋된 chunk 들(약 50%)은 DB 에 그대로 남고 Job 은 FAILED.
     *
     * @param failRatio 0~1 사이. 0.5 면 절반 처리 후 실패.
     */
    public SettleReport runFailing(double failRatio) {
        long remaining = orderRepository.count() - settlementRepository.count();
        long failAfter = Math.max(1, Math.round(remaining * failRatio));
        faultBox.arm(failAfter);

        JobParameters params = newRunParams();
        log.warn("[BATCH] 장애 주입 실행 — 남은 {}건 중 {}건 처리 후 실패 예정", remaining, failAfter);
        return launch(params, "batch-fail");
    }

    /**
     * [Step3-2] <b>같은 JobParameters(runId)</b>로 실행 → Spring Batch 네이티브 재시작.
     *
     * <p>처음엔 {@code failRatio} 를 줘서 50%에서 실패시키고(같은 runId), 두 번째엔 {@code failRatio=null}
     * 로 같은 runId 를 다시 실행한다. 그러면 새 인스턴스가 아니라 <b>실패한 그 인스턴스를 이어서</b>
     * 재개한다 — Reader 가 마지막 커밋 페이지부터 다시 읽으므로 앞 50%는 재독조차 하지 않는다.
     *
     * @param runId     재시작의 키. 같은 값이면 "이어서", 다른 값이면 "새로".
     * @param failRatio null 이면 정상(=장애 해제), 값이 있으면 그 비율에서 실패.
     */
    public SettleReport runRestartable(long runId, Double failRatio) {
        if (failRatio == null) {
            faultBox.disarm();
        } else {
            long remaining = orderRepository.count() - settlementRepository.count();
            faultBox.arm(Math.max(1, Math.round(remaining * failRatio)));
        }

        // runId 만 식별 파라미터로 → 같은 runId = 같은 JobInstance = 재시작 대상
        JobParameters params = new JobParametersBuilder()
                .addLong("runId", runId)
                .toJobParameters();
        log.warn("[BATCH] 재시작 가능 실행 — runId={}, 장애={}", runId, faultBox.armed());
        return launch(params, "batch-restart");
    }

    /** 공통 실행 + 리포트 작성. start(job, params) 는 실패한 인스턴스면 자동으로 재시작한다. */
    private SettleReport launch(JobParameters params, String label) {
        try (HeapMonitor monitor = HeapMonitor.start(label, 500)) {
            long startedAt = System.currentTimeMillis();
            JobExecution execution = jobOperator.start(settlementJob, params);

            long read = 0, written = 0, skipped = 0;
            for (StepExecution step : execution.getStepExecutions()) {
                read += step.getReadCount();
                written += step.getWriteCount();
                skipped += step.getFilterCount();
            }
            long elapsed = System.currentTimeMillis() - startedAt;

            SettleReport report = new SettleReport(read, written, skipped, elapsed,
                    monitor.peakUsedMb(), monitor.maxHeapMb(), execution.getStatus().toString());
            log.warn("[BATCH] 종료. status={}, 리포트={}", execution.getStatus(), report);
            return report;
        } catch (JobInstanceAlreadyCompleteException e) {
            throw new IllegalStateException(
                    "이미 완료된 정산 인스턴스입니다(같은 파라미터). 새 runId 를 쓰거나 DELETE /settlements 후 다시 시작하세요.", e);
        } catch (Exception e) {
            throw new IllegalStateException("정산 배치 실행 실패: " + e.getMessage(), e);
        }
    }

    private JobParameters newRunParams() {
        return new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }
}
