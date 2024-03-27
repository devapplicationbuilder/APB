#!/bin/bash

set -e

export USER_ID=${QUICKDEV_PUID:=9001}
export GROUP_ID=${QUICKDEV_PGID:=9001}
export API_HOST="${QUICKDEV_API_SERVICE_URL:=http://localhost:8080}"

# Run init script
echo "Initializing node-service..."
/quickdev/node-service/init.sh

cd /quickdev/node-service/app

echo
echo "Running quickdev node-service with:"
echo "  API service host: ${API_HOST}"
echo "           user id: ${USER_ID}"
echo "          group id: ${GROUP_ID}"
echo

exec gosu ${USER_ID}:${GROUP_ID}  yarn start

