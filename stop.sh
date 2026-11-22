#!/bin/sh
set -eu
docker compose --profile prod down --volumes --remove-orphans
