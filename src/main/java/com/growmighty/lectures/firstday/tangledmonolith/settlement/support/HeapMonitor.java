package com.growmighty.lectures.firstday.tangledmonolith.settlement.support;

import lombok.extern.slf4j.Slf4j;

// 백그라운드 스레드로 일정 주기마다 힙 사용량을 로그로 찍는 클래스
@Slf4j
public final class HeapMonitor implements AutoCloseable {

    private static final long MB = 1024 * 1024;

    private final String label;
    private final long intervalMs;
    private final long startedAt;
    private final Thread thread;

    private volatile boolean running = true;
    private volatile long peakUsedMb = 0;

    private HeapMonitor(String label, long intervalMs) {
        this.label = label;
        this.intervalMs = intervalMs;
        this.startedAt = System.currentTimeMillis();
        this.thread = new Thread(this::loop, "heap-monitor-" + label);
        this.thread.setDaemon(true);
    }

    public static HeapMonitor start(String label, long intervalMs) {
        HeapMonitor monitor = new HeapMonitor(label, intervalMs);
        monitor.thread.start();
        return monitor;
    }

    private void loop() {
        while (running) {
            sample();
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void sample() {
        Runtime rt = Runtime.getRuntime();
        long max = rt.maxMemory() / MB;              // -Xmx
        long used = (rt.totalMemory() - rt.freeMemory()) / MB;
        long elapsed = System.currentTimeMillis() - startedAt;
        if (used > peakUsedMb) {
            peakUsedMb = used;
        }
        int percent = max > 0 ? (int) (used * 100 / max) : 0;
        log.warn("[mem:{}] used={}MB / max={}MB ({}%) {} elapsed={}ms",
            label, used, max, percent, bar(percent), elapsed);
    }

    /** used 비율을 막대그래프로 (콘솔에서 차오르는 게 보이도록) */
    private static String bar(int percent) {
        int filled = Math.min(20, percent / 5);
        return "[" + "#".repeat(filled) + "-".repeat(20 - filled) + "]";
    }

    public long peakUsedMb() {
        return peakUsedMb;
    }

    public long maxHeapMb() {
        return Runtime.getRuntime().maxMemory() / MB;
    }

    @Override
    public void close() {
        running = false;
        thread.interrupt();
        sample(); // 종료 직전 마지막 스냅샷
        log.warn("[mem:{}] 종료. peakUsed={}MB / max={}MB", label, peakUsedMb, maxHeapMb());
    }
}
