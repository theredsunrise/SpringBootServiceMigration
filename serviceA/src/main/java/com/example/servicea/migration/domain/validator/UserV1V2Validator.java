package com.example.servicea.migration.domain.validator;

import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV2.domain.UserV2;

import java.util.Objects;

public class UserV1V2Validator {

    public void validate(UserV1 userV1, UserV2 userV2) throws ValidatorException {
        if (userV1 == null || userV2 == null) {
            throw new IllegalStateException("UserV1 or UserV2 is null.");
        }
        assertEqual(userV1.id(), userV2.id(),
                "IDs do not match for UserV1-> id: %s and UserV2-> id: %s."
                        .formatted(userV1.id(), userV2.id())
        );

        assertEqual(userV1.username(), userV2.username(),
                "Usernames do not match for UserV1-> id: %s username: %s and UserV2-> id: %s username: %s."
                        .formatted(
                                userV1.id(), userV2.username(),
                                userV2.id(), userV2.username()
                        )
        );

        assertEqual(userV1.createdAt(), userV2.createdAt(),
                "CreatedAt timestamps do not match for UserV1-> id: %s createdAt: %s and UserV2-> id: %s createdAt: %s."
                        .formatted(
                                userV1.id(), userV2.createdAt(),
                                userV2.id(), userV2.createdAt()
                        ));

        assertEqual(userV1.updatedAt(), userV2.updatedAt(),
                "UpdatedAt timestamps do not match for UserV1-> id: %s updatedAt: %s and UserV2-> id: %s updatedAt: %s."
                        .formatted(
                                userV1.id(), userV2.updatedAt(),
                                userV2.id(), userV2.updatedAt()
                        )
        );

        var nameV1 = userV1.name().replaceAll("\\s+", "").strip();
        var nameV2 = (userV2.name().strip() + userV2.surname().strip()).replaceAll("\\s+", "");
        assertEqual(nameV1, nameV2, "Names do not match for UserV1-> id: %s name: %s and UserV2-> id: %s name: %s."
                .formatted(
                        userV1.id(), nameV1,
                        userV2.id(), nameV2
                )
        );
    }

    private void assertEqual(Object obj1, Object obj2, String errorMessage) throws ValidatorException {
        if (!Objects.equals(obj1, obj2)) {
            throw new ValidatorException(errorMessage);
        }
    }
}
