package com.example.servicea.migration.infrastructure.adapter.out.validation.mapper;

import com.example.servicea.migration.domain.model.ValidationJobAttributes;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import java.util.function.ToLongFunction;

@Component
public class ValidationJobAttrMapper {

    public ValidationJobAttributes fromJobExecution(JobExecution execution) {
        var jobInstance = execution.getJobInstance();
        var exitStatus = execution.getExitStatus();

        return new ValidationJobAttributes(
                execution.getId(),
                jobInstance.getInstanceId(),
                jobInstance.getJobName(),
                execution.getStatus().name(),
                exitStatus.getExitCode(),
                exitStatus.getExitDescription(),
                execution.getCreateTime(),
                execution.getStartTime(),
                execution.getEndTime(),
                execution.getLastUpdated(),
                sum(execution, StepExecution::getReadCount),
                sum(execution, StepExecution::getFilterCount),
                sum(execution, StepExecution::getWriteCount),
                sum(execution, StepExecution::getCommitCount),
                sum(execution, StepExecution::getRollbackCount),
                sum(execution, StepExecution::getSkipCount)
        );
    }

    private long sum(JobExecution execution, ToLongFunction<StepExecution> count) {
        return execution.getStepExecutions().stream().mapToLong(count).sum();
    }
}
