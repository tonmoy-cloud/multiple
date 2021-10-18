FROM openjdk:8
MAINTAINER lab.infoworks.com

ADD auth-service/target/*.jar ROOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ROOT.jar"]
