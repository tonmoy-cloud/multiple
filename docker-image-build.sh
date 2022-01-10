#!/bin/bash

echo 'Creating First-Service Image'
docker image build -f first-service/Dockerfile-self -t first-web-service:1.0 ./first-service/
#docker image push <docker-hub-repository>:first-web-service-1.0

echo 'Creating Second-Service Image'
docker image build -f second-service/Dockerfile-self -t second-web-service:1.0 ./second-service/
#docker image push <docker-hub-repository>:second-web-service-1.0

echo 'Creating Auth-Service Image'
docker image build -f auth-service/Dockerfile-self -t auth-web-service:1.0 ./auth-service/
#docker image push <docker-hub-repository>:auth-web-service-1.0

echo 'Creating Gateway-Service Image'
docker image build -f gateway-service-config/Dockerfile-self -t gateway-service:1.0 ./gateway-service-config/
#docker image push <docker-hub-repository>:gateway-service-1.0

echo 'Creating Monitoring Image'
docker image build -f monitoring/Dockerfile -t prometheus-db:1.0 ./monitoring/
#docker image push <docker-hub-repository>:prometheus-db-1.0