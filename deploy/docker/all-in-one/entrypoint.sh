#!/bin/bash

set -e

export USER_ID=${QUICKDEV_PUID:=9001}
export GROUP_ID=${QUICKDEV_PGID:=9001}

# Update ID of lowcoder user if required
if [ ! `id --user quickdev` -eq ${USER_ID} ]; then
    usermod --uid ${USER_ID} quickdev
    echo "ID for lowcoder user changed to: ${USER_ID}"
fi;

# Update ID of lowcoder group if required
if [ ! `id --group quickdev` -eq ${GROUP_ID} ]; then
    groupmod --gid ${GROUP_ID} quickdev
    echo "ID for lowcoder group changed to: ${GROUP_ID}"
fi;

# Update host on which mongo is supposed to listen
# If QUICKDEV_MONGODB_EXPOSED is true, it will isten on all interfaces
if [ "${QUICKDEV_MONGODB_EXPOSED}" = "true" ]; then
    export MONGO_LISTEN_HOST="0.0.0.0"
else
    export MONGO_LISTEN_HOST="127.0.0.1"
fi;

LOGS="/quickdev-stacks/logs"
DATA="/quickdev-stacks/data"
CERT="/quickdev-stacks/ssl"
# Create folder for holding application logs and data
mkdir -p ${LOGS}/redis \
  ${LOGS}/mongodb \
  ${LOGS}/api-service \
  ${LOGS}/node-service \
  ${LOGS}/frontend \
  ${DATA}/redis \
  ${DATA}/mongodb \
  ${CERT}

# Update owner of logs and data
chown -R ${USER_ID}:${GROUP_ID} /quickdev-stacks/ /quickdev/etc

# Enable services
SUPERVISOR_AVAILABLE="/quickdev/etc/supervisord/conf-available"
SUPERVISOR_ENABLED="/quickdev/etc/supervisord/conf-enabled"

# Create folder for supervisor conf-enabled
mkdir -p ${SUPERVISOR_ENABLED}

# Recreate links to enabled services
rm -f ${SUPERVISOR_ENABLED}/*.conf

# Enable redis if configured to run
if [ "${QUICKDEV_REDIS_ENABLED:=true}" = "true" ]; then
    ln ${SUPERVISOR_AVAILABLE}/01-redis.conf ${SUPERVISOR_ENABLED}/01-redis.conf
fi;

# Enable mongodb if configured to run
if [ "${QUICKDEV_MONGODB_ENABLED:=true}" = "true" ]; then
    ln ${SUPERVISOR_AVAILABLE}/02-mongodb.conf ${SUPERVISOR_ENABLED}/02-mongodb.conf
fi;

# Enable api-service if configured to run
if [ "${QUICKDEV_API_SERVICE_ENABLED:=true}" = "true" ]; then
    ln ${SUPERVISOR_AVAILABLE}/10-api-service.conf ${SUPERVISOR_ENABLED}/10-api-service.conf
fi;

# Enable node-service if configured to run
if [ "${QUICKDEV_NODE_SERVICE_ENABLED:=true}" = "true" ]; then
    ln ${SUPERVISOR_AVAILABLE}/11-node-service.conf ${SUPERVISOR_ENABLED}/11-node-service.conf
fi;

# Enable frontend if configured to run
if [ "${QUICKDEV_FRONTEND_ENABLED:=true}" = "true" ]; then
   ln ${SUPERVISOR_AVAILABLE}/20-frontend.conf ${SUPERVISOR_ENABLED}/20-frontend.conf
fi;

# Handle CMD command
"$@"
