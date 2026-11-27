package com.example.servicea.performance;

import com.example.servicea.migration.domain.entity.MigrationState;
import com.example.servicea.migration.infrastructure.adapter.in.web.dto.MigrationStateRequest;
import com.example.servicea.userV2.infrastructure.adapter.in.web.dto.UserV2WriteRequest;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class UserV2SyncSimulation extends Simulation {
    private static final int VIRTUAL_USER_COUNT = 10;
    private static final int USER_COUNT_PER_USER = 1000;
    private static final Duration BARRIER_TIMEOUT = Duration.ofMinutes(2);
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    private final String runId = Long.toString(System.currentTimeMillis());
    private final AtomicInteger userNumberSequence = new AtomicInteger();

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private final ScenarioBuilder createV2UsersScenario = scenario(
            "10 users create 10000 V2 users with V1 synchronization")
            .exec(session -> {
                int userNumber = userNumberSequence.incrementAndGet();
                return session
                        .set("runId", runId)
                        .set("userNumber", userNumber)
                        .set("userSuffix", "vu" + userNumber)
                        .set("createdCount", 0);
            })
            .exec(rendezVous(VIRTUAL_USER_COUNT, BARRIER_TIMEOUT))
            .doIf(session -> session.getInt("userNumber") == 1)
            .then(exec(http("activate-v2-and-sync")
                    .put("/api/migration")
                    .body(StringBody(toJson(new MigrationStateRequest(MigrationState.ActivatedV2AndSync))))
                    .asJson()
                    .check(status().is(200))))
            .exec(rendezVous(VIRTUAL_USER_COUNT, BARRIER_TIMEOUT))
            .exec(exitHereIfFailed())
            .exec(repeat(USER_COUNT_PER_USER).on(
                    exec(session -> {
                        int number = session.getInt("createdCount") + 1;
                        String username = "gatling-v2-" + session.getString("runId")
                                + "-" + session.getString("userSuffix") + "-" + number;
                        return session.set("createdCount", number).set("username", username);
                    })
                            .exec(http("create-v2-user")
                                    .post("/api/v2/users")
                                    .body(StringBody(session -> toJson(new UserV2WriteRequest(
                                            session.getString("username"),
                                            "User " + session.getInt("createdCount"),
                                            "Simulation"))))
                                    .asJson()
                                    .check(status().is(201)))
                            .exec(exitHereIfFailed())));

    {
        setUp(createV2UsersScenario.injectOpen(atOnceUsers(VIRTUAL_USER_COUNT))).protocols(httpProtocol);
    }

    private static String toJson(Object request) {
        return OBJECT_MAPPER.writeValueAsString(request);
    }
}
