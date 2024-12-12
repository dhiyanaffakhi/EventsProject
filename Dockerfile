FROM openjdk:17
EXPOSE 8087
COPY target/eventsProject-1.0.0-SNAPSHOT.jar eventsProject-1.0.0-SNAPSHOT.jar
ENTRYPOINT ["java","-jar", "/eventsProject-1.0.0-SNAPSHOT.jar"]
