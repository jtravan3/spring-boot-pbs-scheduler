# Start with a base image containing Java runtime
FROM openjdk:8-jdk-alpine

# Add Maintainer Info
LABEL maintainer="ravanj1@citadel.edu"

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 5000 available to the world outside this container
EXPOSE 5000

# The application's jar file
ARG JAR_FILE=target/spring-boot-pbs-scheduler-0.0.1-SNAPSHOT.jar

# Add the application's jar to the container
ADD ${JAR_FILE} spring-boot-pbs-scheduler.jar

# Run the jar file
ENTRYPOINT ["java","-Xmx1024m","-jar","/spring-boot-pbs-scheduler.jar"]
