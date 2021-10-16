FROM openjdk:11

MAINTAINER Nadir Tariverdiyev

EXPOSE 8882

WORKDIR /usr/local/bin/

COPY target/apigateway-0.0.1-SNAPSHOT.jar apigateway-server.jar

CMD ["java", "-jar", "apigateway-server.jar"]