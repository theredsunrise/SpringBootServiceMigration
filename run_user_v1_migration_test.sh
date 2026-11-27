#!/bin/sh
set -eu
./mvnw -pl serviceA gatling:test -Dgatling.simulationClass="com.example.servicea.performance.UserV1MigrationSimulation"
