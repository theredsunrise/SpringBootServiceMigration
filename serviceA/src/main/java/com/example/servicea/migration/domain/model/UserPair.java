package com.example.servicea.migration.domain.model;

import com.example.servicea.userV1.domain.UserV1;
import com.example.servicea.userV2.domain.UserV2;

public record UserPair(UserV1 userV1, UserV2 userV2) {
}
