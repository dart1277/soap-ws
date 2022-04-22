package com.cx.client.api.filter.apiratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
class RateLimiter {
    private static final int MAX_RETRY_CNT = 10;
    private static final int REQS_PER_SECOND = 8;
    private static final int DELAY_MS = 1000;
    private final ScheduledExecutorService executorService;
    private final BlockingQueue<Long> rateLimitQueue;
    private final ReentrantLock rateLimitQueueReadLock = new ReentrantLock();
    private final RequestPrefixEnum requestPrefixEnum;

    public RateLimiter(RequestPrefixEnum requestPrefixEnum) {
        this.requestPrefixEnum = requestPrefixEnum;
        rateLimitQueue = new ArrayBlockingQueue<>(REQS_PER_SECOND, true);
        executorService = Executors.newSingleThreadScheduledExecutor();
        RateLimitTokenProvider rateLimitTokenProvider = new RateLimitTokenProvider(rateLimitQueue, requestPrefixEnum);
        executorService.scheduleAtFixedRate(rateLimitTokenProvider, DELAY_MS, DELAY_MS, TimeUnit.MILLISECONDS);
    }

    public void rateLimitRequest() {
        getToken();
    }

    private long getToken() {
        Long token;
        int attemptCnt = 0;
        while (Objects.isNull(token = takeToken())) {
            if (attemptCnt >= MAX_RETRY_CNT) {
                String errorMessage = "Maximum retry count of: " + MAX_RETRY_CNT + " has been reached!";
                log.error(errorMessage);
                throw new IllegalStateException("Rate limiting failed for: " + requestPrefixEnum.prefix + ": " + errorMessage); // 429
            }
            ++attemptCnt;
        }
        return token;
    }

    private Long takeToken() {
        rateLimitQueueReadLock.lock();
        Long token;
        try {
            long now = System.currentTimeMillis();
            if(rateLimitQueue.isEmpty()) {
                log.error("waiting for queue to be full");
            }
            token = rateLimitQueue.take();
            while (token < now && !rateLimitQueue.isEmpty()) {
                log.error("clearing timestamp: " + token.toString());
                token = rateLimitQueue.take();
            }
        } catch (InterruptedException e) {
            token = null;
        } finally {
            rateLimitQueueReadLock.unlock();
        }
        return token;
    }

    @RequiredArgsConstructor
    private static class RateLimitTokenProvider implements Runnable {

        private final BlockingQueue<Long> rateLimitQueue;
        private final RequestPrefixEnum requestPrefixEnum;

        @Override
        public void run() {
            addTokensToQueue();
        }

        private void addTokensToQueue() {
            long now = System.currentTimeMillis();
            rateLimitQueue.clear();
            for (int requestCnt = 1; requestCnt <= REQS_PER_SECOND; ++requestCnt) {
                long scheduledRequestTime = now + (DELAY_MS * requestCnt) / REQS_PER_SECOND;
                try {
                    rateLimitQueue.put(scheduledRequestTime);
                } catch (InterruptedException ex) {
                    log.error("Rate limit failed to schedule " + requestPrefixEnum.prefix + " at: " + scheduledRequestTime, ex);
                }
            }
        }
    }

    @PreDestroy
    private void cleanUp() {
        log.info("Shutting down " + RateLimiter.class.getName());
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(DELAY_MS, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
        }
    }
}
