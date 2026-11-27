package com.example.servicea.performance;

import com.example.servicea.migration.domain.entity.MigrationState;
import com.example.servicea.migration.infrastructure.adapter.in.web.dto.MigrationStateRequest;
import com.example.servicea.userV1.infrastructure.adapter.in.web.dto.UserV1WriteRequest;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class UserV1MigrationSimulation extends Simulation {
    private static final int VIRTUAL_USER_COUNT = 10;
    private static final int USER_COUNT_PER_USER = 2000;
    private static final int USERS_BEFORE_MIGRATION_PER_USER = 1500;
    private static final int UPDATE_COUNT_PER_USER = 500;
    private static final Duration BARRIER_TIMEOUT = Duration.ofMinutes(2);
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    private final String runId = Long.toString(System.currentTimeMillis());
    private final AtomicInteger userNumberSequence = new AtomicInteger();

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private ChainBuilder createUsersTask(int count) {
        return repeat(count).on(
                exec(session -> {
                    int number = session.getInt("createdCount") + 1;
                    String username = "gatling-" + session.getString("runId")
                            + "-" + session.getString("userSuffix") + "-" + number;
                    return session.set("createdCount", number).set("username", username);
                })
                        .exec(http("create-user")
                                .post("/api/v1/users")
                                .body(StringBody(session -> toJson(new UserV1WriteRequest(
                                        session.getString("username"),
                                        "User " + session.getInt("createdCount")))))
                                .asJson()
                                .check(status().is(201), jsonPath("$.id").saveAs("createdId")))
                        .exec(exitHereIfFailed())
                        .exec(session -> {
                            List<String> users = new ArrayList<>(session.getList("users"));
                            users.add(session.getString("createdId") + ":" + session.getString("username"));
                            return session.set("users", users);
                        }));
    }

    private final ChainBuilder activateStartV2 = doIf(session -> session.getInt("userNumber") == 1)
            .then(exec(http("activate-start-userV2")
                    .put("/api/migration")
                    .body(StringBody(toJson(new MigrationStateRequest(MigrationState.StartV2))))
                    .asJson()
                    .check(status().is(200))));

    private final ChainBuilder updateUsersTask = foreach("#{usersToUpdate}", "user")
            .on(
                    exec(session -> {
                        String[] parts = session.getString("user").split(":", 2);
                        return session.set("updateId", parts[0]).set("updateUsername", parts[1]);
                    })
                            .exec(http("update-user-from-last")
                                    .put("/api/v1/users/#{updateId}")
                                    .body(StringBody(session -> toJson(new UserV1WriteRequest(
                                            session.getString("updateUsername"),
                                            "Updated " + session.getString("updateUsername")))))
                                    .asJson()
                                    .check(status().is(200)))
                            .exec(exitHereIfFailed()));

    private final ScenarioBuilder loadUsersScenario = scenario(
            "10 users create 20000 V1 users, then update 5000 in reverse")
            .exec(session -> {
                int userNumber = userNumberSequence.incrementAndGet();
                return session
                        .set("runId", runId)
                        .set("userNumber", userNumber)
                        .set("userSuffix", "vu" + userNumber)
                        .set("createdCount", 0)
                        .set("users", new ArrayList<String>());
            })
            .exec(createUsersTask(USERS_BEFORE_MIGRATION_PER_USER))
            .exec(activateStartV2)
            .exec(createUsersTask(USER_COUNT_PER_USER - USERS_BEFORE_MIGRATION_PER_USER))
            .exec(rendezVous(VIRTUAL_USER_COUNT, BARRIER_TIMEOUT))
            .exec(exitHereIfFailed())
            .exec(session -> {
                List<String> users = new ArrayList<>(session.getList("users"));
                Collections.reverse(users);
                return session.set("usersToUpdate", new ArrayList<>(users.subList(0, UPDATE_COUNT_PER_USER)));
            })
            .exec(updateUsersTask);

    {
        setUp(loadUsersScenario.injectOpen(atOnceUsers(VIRTUAL_USER_COUNT))).protocols(httpProtocol);
    }

    private static String toJson(Object request) {
        return OBJECT_MAPPER.writeValueAsString(request);
    }
}
