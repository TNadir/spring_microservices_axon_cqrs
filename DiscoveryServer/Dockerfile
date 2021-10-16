FROM openjdk:11

MAINTAINER Nadir Tariverdiyev

EXPOSE 8761

WORKDIR /usr/local/bin/

COPY target/discoveryserver-0.0.1-SNAPSHOT.jar discovery-server.jar

CMD ["java", "-jar", "discovery-server.jar"]