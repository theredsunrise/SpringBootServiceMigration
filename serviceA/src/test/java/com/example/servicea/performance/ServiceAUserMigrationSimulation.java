package com.example.servicea.performance;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ServiceAUserMigrationSimulation extends Simulation {
    private static final int USER_COUNT = 8000;
    private static final int MIGRATION_AFTER = 6000;
    private static final int UPDATE_COUNT = 2000;
    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");

    private final String runId = Long.toString(System.currentTimeMillis());

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private final ChainBuilder createUsersTask = repeat(USER_COUNT).on(
            exec(session -> {
                int number = session.getInt("createdCount") + 1;
                String username = "gatling-" + session.getString("runId") + "-" + number;
                return session.set("createdCount", number).set("username", username);
            })
                    .exec(http("create-user")
                            .post("/api/v1/users")
                            .body(StringBody(session -> "{\"username\":\""
                                    + session.getString("username")
                                    + "\",\"name\":\"User "
                                    + session.getInt("createdCount")
                                    + "\"}"))
                            .asJson()
                            .check(status().is(201), jsonPath("$.id").saveAs("createdId")))
                    .exec(exitHereIfFailed())
                    .exec(session -> {
                        List<String> users = new ArrayList<>(session.getList("users"));
                        users.add(session.getString("createdId") + ":" + session.getString("username"));
                        return session.set("users", users);
                    })
                    .doIf(session -> session.getInt("createdCount") == MIGRATION_AFTER).then(
                            exec(http("activate-start-userV2")
                                    .put("/api/migration")
                                    .body(StringBody("{\"state\":\"StartV2\"}"))
                                    .asJson()
                                    .check(status().is(200)))
                                    .exec(exitHereIfFailed())));

    private final ChainBuilder updateUsersTask = foreach("#{usersToUpdate}", "user").on(
            exec(session -> {
                String[] parts = session.getString("user").split(":", 2);
                return session.set("updateId", parts[0]).set("updateUsername", parts[1]);
            })
                    .exec(http("update-user-from-last")
                            .put("/api/v1/users/#{updateId}")
                            .body(StringBody(session -> "{\"username\":\""
                                    + session.getString("updateUsername")
                                    + "\",\"name\":\"Updated "
                                    + session.getString("updateUsername")
                                    + "\"}"))
                            .asJson()
                            .check(status().is(200)))
                    .exec(exitHereIfFailed()));

    private final ScenarioBuilder loadUsersScenario = scenario(
            "Create 8000 users, activate V2 at 6000, update 500 in reverse")
            .exec(session -> session
                    .set("runId", runId)
                    .set("createdCount", 0)
                    .set("users", new ArrayList<String>()))
            .exec(createUsersTask)
            .exec(session -> {
                List<String> users = new ArrayList<>(session.getList("users"));
                Collections.reverse(users);
                return session.set("usersToUpdate", new ArrayList<>(
                        users.subList(0, Math.min(UPDATE_COUNT, users.size()))));
            })
            .exec(updateUsersTask);

    {
        setUp(loadUsersScenario.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }
}
