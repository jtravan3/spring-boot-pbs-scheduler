# Start with a base image containing Maven
FROM maven:3.6.0-alpine AS base
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
ADD . /usr/src/app
RUN mvn clean install

# Run execution from open-jdk image
FROM openjdk:8-jdk-alpine

# Add Maintainer Info
LABEL maintainer="ravanj1@citadel.edu"

# Make port 5000 available to the world outside this container
EXPOSE 5000

# Copy processed .jar file from base image
COPY --from=base /usr/src/app/target/spring-boot-pbs-scheduler-0.0.1-SNAPSHOT.jar spring-boot-pbs-scheduler.jar
