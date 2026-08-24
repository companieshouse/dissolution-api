#!/bin/bash
#
# Start script for dissolution-api

PORT="${PORT:-8080}"

exec java -jar -Dserver.port="${PORT}" "dissolution-api.jar"
