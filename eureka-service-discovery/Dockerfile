FROM openjdk:8
MAINTAINER lab.infoworks.com

ADD eureka-service-discovery/target/*.jar ROOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ROOT.jar"]
