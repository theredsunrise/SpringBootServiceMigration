# SpringBootServiceMigration

A public example of a phased migration from a `users_v1` table to a `users_v2` table in a Spring Boot service. It demonstrates how to keep writes available while preparing the new schema, backfilling existing records, validating the copy, and switching traffic to V2.

## Migration flow

The rollout is controlled by a persisted migration state, changed by an operator through the migration API:

1. **`ActivatedV1`** — V1 is available; V2 is unavailable.
2. **`StartV2`** — V1 remains available and new V1 writes are copied to V2; V2 is unavailable to clients.
3. **`BackFillV2`** — V1 remains available and continues syncing writes to V2. A scheduled job copies existing V1 records that do not yet exist in V2; V2 is unavailable to clients.
4. **`ActivatedV2AndSync`** — after backfill and validation have been reviewed, V2 becomes available and V1 is unavailable. V2 writes are also synchronized to V1 during this transition period.
5. **`ActivatedV2`** — V2 remains available, V1 remains unavailable, and synchronization to V1 stops.

The application records backfill and validation status, but does not automatically advance migration states when a job finishes. Operators review the results and request each state transition. `ActivatedV2` can be moved back to `ActivatedV1` if a rollback is needed.

Backfill batch results, including their status and ID range, are available at `GET /api/migration/backfill-statuses`. During `BackFillV2` and later V2 states, a Spring Batch job validates that V1 and V2 records exist and that their IDs, usernames, timestamps, and mapped names match. Start it with `POST /api/migration/start-validation-job`; check execution progress at `GET /api/migration/validation-job-status` and record mismatches at `GET /api/migration/validation-statuses`.

V1 stores a single `name`; the example maps it to V2 `name` and `surname` by splitting on the last space. The reverse mapping joins those fields with a space.

## Run locally

Requirements: Java 21, Maven, and Docker Compose.

Create a `.env` file in the repository root to override the local defaults below. Compose can also start without it. The default database name, username, and password are all `servicea`; change them for your environment, especially outside local development.

```dotenv
DB_NAME=servicea
DB_USERNAME=servicea
DB_PASSWORD=servicea
PORT=8080
```

`DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, and `PORT` are configurable and default to `servicea`, `servicea`, `servicea`, and `8080`. PostgreSQL uses the fixed port `5432`; Redis uses host `redis` and port `6379`. `PORT` sets both the service's listening port and the published HTTP port. Spring CORS allows `http://localhost:<PORT>`, derived from the `server.port` application property.

Then run:

```sh
./start.sh
```

This builds the application and starts the service, PostgreSQL, and Redis containers. The API is available at `http://localhost:<PORT>`, using the `PORT` value from `.env`; Swagger UI is at `/swagger-ui/index.html`.

To stop the stack and remove its containers and PostgreSQL volume:

```sh
./stop.sh
```

## APIs

- V1 users: `/api/v1/users`
- V2 users: `/api/v2/users`
- Migration state and job status: `/api/migration`

The user APIs provide create, list, get, update, patch, and delete operations. User lists support pagination.

