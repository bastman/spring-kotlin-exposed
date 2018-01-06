FROM openjdk:8-jre-alpine

EXPOSE 8080 8081

COPY build/libs/rest-api-0.0.1.jar /opt/app/app.jar
COPY docker/app/check-health.sh /usr/local/bin/

WORKDIR /opt/app/
CMD java -jar app.jar
