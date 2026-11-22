package com.example.servicea.migration.infrastructure.adapter.out.validation.job;

import com.example.servicea.migration.domain.model.UserPair;
import com.example.servicea.migration.domain.model.ValidationStatus;
import com.example.servicea.migration.domain.validator.UserV1V2Validator;
import com.example.servicea.migration.domain.validator.ValidatorException;
import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV2.domain.UserV2;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component(DefaultValidationJobLauncher.PROCESSOR_NAME)
@RequiredArgsConstructor
public class ValidationProcessor implements ItemProcessor<UserPair, ValidationStatus> {
    private final UserV1V2Validator validator;

    @Override
    public ValidationStatus process(UserPair pair) {
        UserV1 userV1 = pair.userV1();
        UserV2 userV2 = pair.userV2();

        try {
            if (userV1 == null && userV2 == null) {
                return new ValidationStatus("Fatal error, fetched null user entities.");
            }
            if (userV1 == null) {
                return ValidationStatus.UserV1DoesNotExist(userV2);
            }
            if (userV2 == null) {
                return ValidationStatus.UserV2DoesNotExist(userV1);
            }
            validator.validate(userV1, userV2);
        } catch (ValidatorException exception) {
            return new ValidationStatus(exception.getMessage());
        }
        return null;
    }
}
