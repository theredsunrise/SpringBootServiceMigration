#!/bin/sh
set -eu
./mvnw -pl serviceA gatling:test
