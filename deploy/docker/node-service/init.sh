#!/bin/bash

set -e 

NODE_SERVICE_ROOT=/quickdev/node-service

# Update ID of lowcoder user if required
if [ ! `id --user quickdev` -eq ${USER_ID} ]; then
    usermod --uid ${USER_ID} quickdev
    echo "ID for quickdev user changed to: ${USER_ID}"
    DO_CHOWN="true"
fi;

# Update ID of lowcoder group if required
if [ ! `id --group quickdev` -eq ${GROUP_ID} ]; then
    groupmod --gid ${GROUP_ID} quickdev
    echo "ID for quickdev group changed to: ${GROUP_ID}"
    DO_CHOWN="true"
fi;

# Update node-server installation owner
if [ "${DO_CHOWN}" = "true" ]; then
    echo "Changing node-service owner to ${USER_ID}:${GROUP_ID}"
    chown -R ${USER_ID}:${GROUP_ID} ${NODE_SERVICE_ROOT}
fi;

echo "quickdev node-service setup finished."
