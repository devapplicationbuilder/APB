#!/bin/sh

set -e

USER_ID=${QUICKDEV_PUID:=9001}
GROUP_ID=${QUICKDEV_PGID:=9001}
CLIENT_ROOT=/quickdev/client

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

# Update api-server installation owner
if [ "${DO_CHOWN}" = "true" ]; then
    chown -R ${USER_ID}:${GROUP_ID} ${CLIENT_ROOT}
    echo "quickdev client files owner modified."
fi;

