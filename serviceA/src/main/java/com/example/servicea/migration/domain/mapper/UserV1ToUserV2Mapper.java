package com.example.servicea.migration.domain.mapper;

import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV2.domain.UserV2;

public class UserV1ToUserV2Mapper {

    public UserV2 from(UserV1 user) {

        var name = user.name().strip();
        var newName = name;
        var surname = "";

        var splitIndex = name.lastIndexOf(' ');
        if (splitIndex != -1) {
            newName = name.substring(0, splitIndex).strip();
            surname = name.substring(splitIndex + 1).strip();
        }

        return new UserV2(
                user.id(),
                user.username(),
                newName,
                surname,
                user.createdAt(),
                user.updatedAt()
        );
    }

    public UserV1 from(UserV2 user) {
        var name = user.name().strip();
        var surname = user.surname().strip();

        return new UserV1(
                user.id(),
                user.username(),
                name + ' ' + surname,
                user.createdAt(),
                user.updatedAt());
    }
}
