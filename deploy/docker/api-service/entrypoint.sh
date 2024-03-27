#!/bin/bash

set -e

export USER_ID="${QUICKDEV_PUID:=9001}"
export GROUP_ID="${QUICKDEV_PGID:=9001}"

# Run init script
echo "Initializing api-service..."
/quickdev/api-service/init.sh

if [ -z $JAVA_HOME ]; then
    JAVA_HOME=`dirname $(dirname $(readlink -f $(which javac)))`
fi;
APP_JAR="${APP_JAR:=/quickdev/api-service/server.jar}"
JAVA_OPTS="${JAVA_OPTS:=}"
CUSTOM_APP_PROPERTIES="${APP_PROPERTIES}"
CONTEXT_PATH=${CONTEXT_PATH:=/}

echo
echo "Running quickdev api-server with:"
echo "    user id: ${USER_ID}"
echo "   group id: ${GROUP_ID}"
echo "  base path: ${CONTEXT_PATH}"
echo
${JAVA_HOME}/bin/java -version
echo

cd /quickdev/api-service
exec gosu ${USER_ID}:${GROUP_ID} ${JAVA_HOME}/bin/java \
  -Djava.security.egd=file:/dev/./urandom \
  -Dhttps.protocols=TLSv1.1,TLSv1.2 \
  -Dlog4j2.formatMsgNoLookups=true \
  -Dspring.config.location="file:///quickdev/api-service/config/application.yml,file:///quickdev/api-service/config/application-selfhost.yml" \
  --add-opens java.base/java.nio=ALL-UNNAMED \
  ${JAVA_OPTS} \
  -jar "${APP_JAR}" --spring.webflux.base-path=${CONTEXT_PATH} ${CUSTOM_APP_PROPERTIES}

