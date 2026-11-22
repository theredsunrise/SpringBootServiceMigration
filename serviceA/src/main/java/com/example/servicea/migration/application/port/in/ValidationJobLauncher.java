package com.example.servicea.migration.application.port.in;

import com.example.servicea.migration.domain.model.ValidationJobStatus;

public interface ValidationJobLauncher {
    ValidationJobStatus start();
}
