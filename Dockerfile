FROM adoptopenjdk/openjdk11:alpine-jre
MAINTAINER hjkwon
WORKDIR root
RUN apk --no-cache add curl
RUN mkdir -p logs
RUN mkdir -p superService
COPY build/libs/develop-0.0.1-SNAPSHOT.jar management-server.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/management-server.jar"]