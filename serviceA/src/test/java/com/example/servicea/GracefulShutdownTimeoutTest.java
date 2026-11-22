package com.example.servicea;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("shutdown-overrun")
class GracefulShutdownTimeoutTest {
    @Test
    void interruptsTasksThatExceedTheShutdownWaitLimits() throws Exception {
        ConfigurableApplicationContext context = startContext();

        GracefulShutdownTest.TaskProbe taskProbe = context.getBean(GracefulShutdownTest.TaskProbe.class);
        assertThat(context.getEnvironment().getProperty("spring.application.name"))
                .isEqualTo("service-a");
        assertThat(context.getEnvironment().getProperty("spring.task.execution.shutdown.await-termination"))
                .isEqualTo("true");
        assertThat(context.getEnvironment().getProperty("spring.task.execution.shutdown.await-termination-period"))
                .isEqualTo("1s");
        assertThat(context.getEnvironment().getProperty("spring.task.scheduling.shutdown.await-termination-period"))
                .isEqualTo("1s");
        assertThat(context.getEnvironment().getProperty("spring.lifecycle.timeout-per-shutdown-phase"))
                .isEqualTo("1s");
        CompletableFuture<Void> shutdown = null;

        try {
            taskProbe.startAsyncTask();
            assertThat(taskProbe.awaitAsyncStarted()).isTrue();
            assertThat(taskProbe.awaitScheduledStarted()).isTrue();

            shutdown = CompletableFuture.runAsync(context::close);
            shutdown.get(10, TimeUnit.SECONDS);

            assertThat(taskProbe.awaitInterrupted()).isTrue();
            assertThat(taskProbe.awaitCompletion()).isTrue();
            assertThat(context.isActive()).isFalse();
        } finally {
            taskProbe.releaseTasks();

            context.close();
            if (shutdown != null) {
                shutdown.get(10, TimeUnit.SECONDS);
            }
        }
    }

    private ConfigurableApplicationContext startContext() {
        String[] profiles = new DefaultActiveProfilesResolver().resolve(getClass());
        return new SpringApplicationBuilder(GracefulShutdownTest.ShutdownTestApplication.class)
                .sources(GracefulShutdownTest.TestConfig.class)
                .profiles(profiles)
                .run();
    }
}
