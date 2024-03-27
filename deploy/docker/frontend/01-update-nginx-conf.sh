#!/bin/sh

set -e

CERT="/quickdev-stacks/ssl"

rm -f /etc/nginx/nginx.conf
echo "Creating nginx config..."
if [ -e "${CERT}/fullchain.pem" ] && [ -e "${CERT}/privkey.pem" ]; then
   echo "Certificates found, configuring with HTTPS."
   ln -s /etc/nginx/nginx-https.conf /etc/nginx/nginx.conf
   if [ ! -e "${CERT}/dhparam.pem" ]; then
      echo "Diffle-Helmann parameters file not found, generating in now... (this can take some time)"
      openssl dhparam -out "${CERT}/dhparam.pem" 4096
   fi;
else
   echo "Certificates not found, configuring with HTTP."
   ln -s /etc/nginx/nginx-http.conf /etc/nginx/nginx.conf
fi;

sed -i "s@__QUICKDEV_MAX_QUERY_TIMEOUT__@${QUICKDEV_MAX_QUERY_TIMEOUT:=120}@" /etc/nginx/nginx.conf
sed -i "s@__QUICKDEV_MAX_REQUEST_SIZE__@${QUICKDEV_MAX_REQUEST_SIZE:=20m}@" /etc/nginx/nginx.conf
sed -i "s@__QUICKDEV_API_SERVICE_URL__@${QUICKDEV_API_SERVICE_URL:=http://localhost:8080}@" /etc/nginx/nginx.conf
sed -i "s@__QUICKDEV_NODE_SERVICE_URL__@${QUICKDEV_NODE_SERVICE_URL:=http://localhost:6060}@" /etc/nginx/nginx.conf

echo "nginx config updated with:"
echo "    quickdev max upload size: ${QUICKDEV_MAX_REQUEST_SIZE:=20m}"
echo "    quickdev api service URL: ${QUICKDEV_API_SERVICE_URL:=http://localhost:8080}"
echo "   quickdev node service URL: ${QUICKDEV_NODE_SERVICE_URL:=http://localhost:6060}"
