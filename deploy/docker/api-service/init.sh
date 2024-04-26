#!/bin/bash

set -e

API_SERVICE_ROOT=/quickdev/api-service

# Update ID of quickdev user if required
if [ ! `id --user quickdev` -eq ${USER_ID} ]; then
    usermod --uid ${USER_ID} quickdev
    echo "ID for quickdev user changed to: ${USER_ID}"
    DO_CHOWN="true"
fi;

# Update ID of quickdev group if required
if [ ! `id --group quickdev` -eq ${GROUP_ID} ]; then
    groupmod --gid ${GROUP_ID} quickdev
    echo "ID for quickdev group changed to: ${GROUP_ID}"
    DO_CHOWN="true"
fi;

# Update api-server installation owner
if [ "${DO_CHOWN}" = "true" ]; then
    chown -R ${USER_ID}:${GROUP_ID} ${API_SERVICE_ROOT}
fi;

# Link log files to /dev/null
#   - we don't need log files, because all logs are also printed to console
if [ ! -e ${API_SERVICE_ROOT}/logs/main.log ]; then
    ln -s /dev/null ${API_SERVICE_ROOT}/logs/main.log
    chmod 777 ${API_SERVICE_ROOT}/logs/main.log
fi;

if [ ! -e ${API_SERVICE_ROOT}/logs/query-error.log ]; then
    ln -s /dev/null ${API_SERVICE_ROOT}/logs/query-error.log
    chmod 777 ${API_SERVICE_ROOT}/logs/query-error.log
fi;

echo "quickdev api-service setup finished."
