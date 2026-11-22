#!/bin/sh
set -eu
mvn clean package -Dmaven.test.skip=true
docker compose --profile prod build --no-cache
docker compose --profile prod up --build -d
