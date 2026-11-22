package com.example.servicea.shared.infrastructure.adapter.in.web.exception;

import com.example.servicea.migration.application.exception.*;
import com.example.servicea.shared.exception.*;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ControllerDisabledException.class)
    ProblemDetail controllerDisabled(ControllerDisabledException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        problem.setTitle("Controller Disabled");
        return problem;
    }

    @ExceptionHandler(MigrationNotFoundException.class)
    ProblemDetail migrationNotFound(MigrationNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Migration state not found");
        return problem;
    }

    @ExceptionHandler(MigrationConflictException.class)
    ProblemDetail migrationConflict(MigrationConflictException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage());

        problem.setTitle("Migration state conflict");
        return problem;
    }

    @ExceptionHandler(UnsupportedMigrationStepException.class)
    ProblemDetail invalidTransitionState(UnsupportedMigrationStepException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                exception.getMessage());

        problem.setTitle("Invalid migration state transition");
        return problem;
    }

    @ExceptionHandler(ValidationJobStatusUnknownException.class)
    ProblemDetail validationJobStatusUnknown(ValidationJobStatusUnknownException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage());

        problem.setTitle("Validation job status unavailable");
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail invalidFormat(HttpMessageNotReadableException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage());

        problem.setTitle("Invalid request");
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validationProblem(MethodArgumentNotValidException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Request validation failed.");

        problem.setTitle("Invalid request");
        problem.setProperty("errors", exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.toList()));
        return problem;
    }

    @ExceptionHandler({UserV1ConflictException.class, UserV2ConflictException.class})
    ProblemDetail userUnexpectlyModified(RuntimeException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage());

        problem.setTitle("User conflict");
        return problem;
    }

    @ExceptionHandler({UserV1NotFoundException.class, UserV2NotFoundException.class})
    ProblemDetail userNotFound(RuntimeException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage());

        problemDetail.setTitle("User not found");
        return problemDetail;
    }

    @ExceptionHandler(UsernameConflictException.class)
    ProblemDetail usernameConflict(UsernameConflictException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage());

        problemDetail.setTitle("Username conflict");
        return problemDetail;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail optimisticLockConflict(OptimisticLockingFailureException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "The user was changed or deleted by another request. Reload the user and retry.");

        problem.setTitle("Concurrent user modification");
        return problem;
    }
}
