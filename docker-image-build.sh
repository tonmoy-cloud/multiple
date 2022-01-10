#!/bin/bash

AppVersion="1.0"
#
###
FirstServiceName="first-service"
echo "Creating ${FirstServiceName} Image"
docker image build -f ${FirstServiceName}/Dockerfile-self -t ${FirstServiceName}:${AppVersion} ./${FirstServiceName}/
#docker image push <docker-hub-repository>:${FirstServiceName}-${AppVersion}
#
###
SecondServiceName="second-service"
echo "Creating ${SecondServiceName} Image"
docker image build -f ${SecondServiceName}/Dockerfile-self -t ${SecondServiceName}:${AppVersion} ./${SecondServiceName}/
#docker image push <docker-hub-repository>:${SecondServiceName}-${AppVersion}
#
###
AuthServiceName="auth-service"
echo "Creating ${AuthServiceName} Image"
docker image build -f ${AuthServiceName}/Dockerfile-self -t ${AuthServiceName}:${AppVersion} ./${AuthServiceName}/
#docker image push <docker-hub-repository>:${AuthServiceName}-${AppVersion}
#
###
GatewayServiceDir="gateway-service-config"
GatewayServiceName="gateway-service"
echo "Creating ${GatewayServiceName} Image"
docker image build -f ${GatewayServiceDir}/Dockerfile-self -t ${GatewayServiceName}:${AppVersion} ./${GatewayServiceDir}/
#docker image push <docker-hub-repository>:${GatewayServiceName}-${AppVersion}
#
###
MonitoringServiceDir="monitoring"
MonitoringServiceName="prometheus-db"
echo "Creating ${MonitoringService} Image"
docker image build -f ${MonitoringServiceDir}/Dockerfile -t ${MonitoringServiceName}:${AppVersion} ./${MonitoringServiceDir}/
#docker image push <docker-hub-repository>:${MonitoringServiceName}-${AppVersion}