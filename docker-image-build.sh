#!/bin/bash

echo 'Creating First-Service Image'
docker image build -f first-service/Dockerfile-self -t first-web-service:1.0 ./first-service/

echo 'Creating Second-Service Image'
docker image build -f second-service/Dockerfile-self -t second-web-service:1.0 ./second-service/

echo 'Creating Auth-Service Image'
docker image build -f auth-service/Dockerfile-self -t auth-web-service:1.0 ./auth-service/

echo 'Creating Gateway-Service Image'
docker image build -f gateway-service-config/Dockerfile-self -t gateway-service:1.0 ./gateway-service-config/