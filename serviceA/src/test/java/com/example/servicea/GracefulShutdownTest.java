package com.example.servicea;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("shutdown-normal")
class GracefulShutdownTest {
    @Test
    void waitsForRunningAsyncAndScheduledTasksBeforeContextShutdown() throws Exception {
        ConfigurableApplicationContext context = startContext();
        TaskProbe taskProbe = context.getBean(TaskProbe.class);
        taskProbe.startAsyncTask();

        assertThat(taskProbe.awaitAsyncStarted()).isTrue();
        assertThat(taskProbe.awaitScheduledStarted()).isTrue();

        CompletableFuture<Void> shutdown = CompletableFuture.runAsync(context::close);
        try {
            assertThatThrownBy(() -> shutdown.get(200, TimeUnit.MILLISECONDS))
                    .isInstanceOf(TimeoutException.class);
        } finally {
            taskProbe.releaseTasks();
        }

        shutdown.get(10, TimeUnit.SECONDS);
        assertThat(taskProbe.awaitCompletion()).isTrue();
        assertThat(context.isActive()).isFalse();
    }

    private ConfigurableApplicationContext startContext() {
        String[] profiles = new DefaultActiveProfilesResolver().resolve(getClass());
        return new SpringApplicationBuilder(ShutdownTestApplication.class)
                .sources(TestConfig.class)
                .profiles(profiles)
                .run();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {
        @Bean
        TaskProbe taskProbe() {
            return new TaskProbe();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAsync
    @EnableScheduling
    @EnableAutoConfiguration
    static class ShutdownTestApplication {
    }

    static class TaskProbe {
        private final CountDownLatch asyncStarted = new CountDownLatch(1);
        private final CountDownLatch scheduledStarted = new CountDownLatch(1);
        private final CountDownLatch releaseTasks = new CountDownLatch(1);
        private final CountDownLatch completed = new CountDownLatch(2);
        private final CountDownLatch interrupted = new CountDownLatch(2);
        private final AtomicBoolean scheduledTaskStarted = new AtomicBoolean();

        @Async
        public void startAsyncTask() {
            runTask("Async task", asyncStarted);
        }

        @Scheduled(fixedRate = 100)
        public void scheduledTask() {
            if (scheduledTaskStarted.compareAndSet(false, true)) {
                runTask("Scheduled task", scheduledStarted);
            }
        }

        private void runTask(String name, CountDownLatch started) {
            if (started.getCount() == 0) {
                return;
            }
            started.countDown();
            try {
                releaseTasks.await();
                System.out.println("***** EXECUTED: " + name);
            } catch (InterruptedException exception) {
                interrupted.countDown();
                Thread.currentThread().interrupt();
            } finally {
                completed.countDown();
            }
        }

        boolean awaitAsyncStarted() throws InterruptedException {
            return asyncStarted.await(5, TimeUnit.SECONDS);
        }

        boolean awaitScheduledStarted() throws InterruptedException {
            return scheduledStarted.await(5, TimeUnit.SECONDS);
        }

        void releaseTasks() {
            releaseTasks.countDown();
        }

        boolean awaitCompletion() throws InterruptedException {
            return completed.await(5, TimeUnit.SECONDS);
        }

        boolean awaitInterrupted() throws InterruptedException {
            return interrupted.await(5, TimeUnit.SECONDS);
        }
    }
}
