package com.example.servicea.migration.infrastructure.adapter.out.validation.job;

import com.example.servicea.migration.domain.model.UserPair;
import com.example.servicea.migration.domain.model.ValidationStatus;
import com.example.servicea.migration.domain.validator.UserV1V2Validator;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.batch.autoconfigure.BatchTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

import static com.example.servicea.migration.infrastructure.adapter.out.validation.job.DefaultValidationJobLauncher.*;

@Configuration
public class ValidationJobConfig {

    @Bean
    public UserV1V2Validator userV1TV2Validator() {
        return new UserV1V2Validator();
    }

    @Bean(defaultCandidate = false)
    @BatchTaskExecutor
    public TaskExecutor batchTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("batch-");
        executor.setVirtualThreads(true);
        return executor;
    }

    @Bean(name = JOB_NAME)
    Job createValidationJob(JobRepository jobRepository, Step validationStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(validationStep).build();
    }

    @Bean(name = STEP_NAME)
    public Step userValidationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier(READER_NAME) ItemStreamReader<UserPair> validationReader,
            @Qualifier(PROCESSOR_NAME) ItemProcessor<UserPair, ValidationStatus> validationProcessor,
            @Qualifier(WRITER_NAME) ItemWriter<ValidationStatus> ValidationStatusWriter) {

        return new StepBuilder(STEP_NAME, jobRepository)
                .<UserPair, ValidationStatus>chunk(BATCH_SIZE)
                .transactionManager(transactionManager)
                .reader(validationReader)
                .processor(validationProcessor)
                .writer(ValidationStatusWriter)
                .faultTolerant()
                .retry(TransientDataAccessException.class)
                .retryLimit(2)
                .build();
    }
}
