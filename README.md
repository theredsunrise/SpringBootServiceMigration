# ETL services

## Run serviceA with PostgreSQL

Install Java 21, Maven, and Docker Compose. From the repository root, run:

```sh
./start.sh
```

Docker Compose reads the root `.env` file automatically. It contains local-only
settings for the database and published ports. The script packages the service
without running tests, then builds and starts the service and PostgreSQL containers.
The API is available at `http://localhost:8080`.
Swagger UI is available at `/swagger-ui/index.html` and OpenAPI JSON at `/v3/api-docs`.

The root `.env` defines `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_PORT`, and
`PORT`. Edit these values to override the local defaults. `DB_PORT` is the host
port published for PostgreSQL, and `PORT` is the host port published for serviceA.
Keep this file limited to local development settings. PostgreSQL data persists in
the `postgres-data` volume.

### Upgrade an existing database to the V1 table name

The initial Flyway migration now creates `users_v1`, so its checksum and description differ from
the version already recorded by existing installations. Back up the database and stop the old
application before upgrading. Run Flyway `repair` against the same database and migration files
to adopt the rewritten V1 migration metadata. Then rename `users` to `users_v1`, `users_pkey` to
`pk_users_v1`, `uk_users_username` to `uk_users_v1_username`, `idx_users_created_at` to
`idx_users_v1_created_at`, and `idx_users_updated_at` to `idx_users_v1_updated_at`. Start the
updated serviceA application and check the migration history and V1 records. Do not run `repair`
against a database whose migration history has not been reviewed.

To stop the stack and remove its database volume, run:

```sh
./stop.sh
```

## Run tests

Persistence tests use `@DataJpaTest` with an isolated PostgreSQL Testcontainer. They do not
connect to the Compose database or require a manually provisioned test database. Docker must be
running for those tests.

Then run:

```sh
mvn -pl serviceA test
```

