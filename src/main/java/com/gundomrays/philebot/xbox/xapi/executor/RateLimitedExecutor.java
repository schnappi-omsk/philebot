package com.gundomrays.philebot.xbox.xapi.executor;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;


@Service
@SuppressWarnings("UnstableApiUsage")
public class RateLimitedExecutor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitedExecutor.class);

    @Value("${ebot.requestsPerMin}")
    private Double requestsPerMin;

    private final BlockingQueue<XApiTask> tasks = new LinkedBlockingQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private RateLimiter rateLimiter;

    @PostConstruct
    private void init() {
        rateLimiter = RateLimiter.create(requestsPerMin / 65.0);
        executorService.submit(this::run);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void run() {
        while (true) {
            try {
                XApiTask taskHolder = tasks.take();
                Runnable task = taskHolder.getTask();
                rateLimiter.acquire(taskHolder.getPermits());
                task.run();
            } catch (InterruptedException e) {
                log.error("Error in the task. ", e);
            }
        }
    }

    public <T> CompletableFuture<T> submit(Callable<T> callable) {
        return submit(1 ,callable);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public <T> CompletableFuture<T> submit(Integer requestsCount, Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();

        tasks.offer(new XApiTask(requestsCount, () -> {
            try {
                future.complete(callable.call());
            } catch (Exception e) {
                log.error("Error submitting task: ", e);
                future.completeExceptionally(e);
            }
        }));
        log.info("Task was added to queue with rate={}", requestsCount);

        return future;
    }


}
