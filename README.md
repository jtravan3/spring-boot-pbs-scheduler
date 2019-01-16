# Spring Boot Prediction-Based Scheduler Prototype

- Uses Spring Boot 2.0.0-M3 with Spring 5 Framework using Reactor Streams for Reactive REST endpoints
- Uses Spring MVC Style Resource push.

## Setup Instructions

1.) Clone down this repository

2.) Ensure that all tests run successfully and the project can be built via Maven.

```bash
mvn install
```

3.) If you are going to use locally, ensure you have the correct file path set for the output files. These files generally will be
output to the desktop. To change the location change the static variable called `RESEARCH_OUTPUT_FILE_NAME` 
in the file `MetricsAggregator` to the location that you want saved. Right now it is set 
to `/Users/jravan/Desktop/research-output`. This will save 7 files to the Desktop under the names of each
test case (`research-output1.csv`, `research-output2.csv`, etc.)

4.) If you are going to use the Amazon S3 solution for storage, then set the variables in the `application.properties`.

```bash
use_amazon_s3=true
s3_bucket_name=spring-boot-prediction-based-scheduler
s3_access_key_id={Client ID}
s3_access_secret={Client Secret}
```

If you are using the S3 solution, the output will only be saved when the simulation is terminated. A shutdown hook is used
to send out the recorded data to S3 before the temporary files are deleted. On S3 you will see a folder with the current timestamp 
as the folder name. Within the folder there will be 7 files called `output{testCaseNumber}` representing the data obtained from
each test case.

5.) If all tests succeed and the project can be built successfully, navigate to the Spring Boot
Application with the name `PredictionBasedPrototypeApplication.java`. If you're using IntelliJ,
click on the file and click `Run`.

## Docker

1.) If you want to execute via Docker or use Docker to deploy in the cloud for more computing power, then you can use these
commands to build a docker image.

First, to build an image run the command from the project root directory:

```bash
docker-compose build
```

This will build an image from the `Dockerfile` and the `docker-compose.yml`. 

2.) To execute the Docker image run the command.

```bash
docker-compose up
```

With the `Dockerfile` and the Amazon S3 configuration, you can deploy the app to the cloud to use more computing power in order
to execute longer tests.

## Docker Hub Execution

I have a personal Docker Hub repository (https://cloud.docker.com/repository/docker/jtravan3/spring-boot-pbs-scheduler) with the latest image. 
You can pull this image down and run it directly. For access, email me directly at ravanj1@citadel.edu.

Install `docker-compose` to your system

```bash
sudo curl -L "https://github.com/docker/compose/releases/download/1.23.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
```

Set the correct permissions for the executable

```bash
sudo chmod +x /usr/local/bin/docker-compose
```

Once you have access to the Docker Hub repository, copy the following `docker-compose.yml` to the location you wish to run.

```yaml
version: "3"

services:
  spring-boot-pbs-scheduler:
    image: jtravan3/spring-boot-pbs-scheduler
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - 5000:5000
    volumes:
      - ./src:/usr/src/app/src
      - ./target:/usr/src/app/target
      - ./pom.xml:/usr/src/app/pom.xml
    working_dir: /usr/src/app
    command: ["java","-Xmx1024m","-jar","/spring-boot-pbs-scheduler.jar"]
```

After that run the following command from the same directory.

```bash
docker-compose pull jtravan3/spring-boot-pbs-scheduler
```

This will pull the latest image to your local machine. Once you have the latest image execute:

```bash
docker-compose up
```

You can perform these commands in a pre-built digital ocean container so that your resources 
are not consumed locally.

## Local Execution

1.) With the application running navigate to a web browser and access the 
URL `http://localhost:5000/rest/pbs/start/difftrans/so/{testCaseNumber}` where `testCaseNumber`
is a number between 1 and 7. See the test cases mapped out below. The numbers represent the percentages
of the category of transactions in the batch tested.

| Test Case # | HCHE | HCLE | LCHE | LCLE |
|-------------|------| -----|------|------|
|      1      | 100  |  0   |  0   |  0   | 
|      2      | 75   |  25  |  0   |  0   | 
|      3      | 50   |  25  |  25  |  0   | 
|      4      | 25   | 25   |  25  |  25  | 
|      5      | 0    |  25  | 25   |  50  | 
|      6      | 0    |  0   |  25  |  75  | 
|      7      | 0    |  0   |  0   |  100 |      


2.) To run a loop of tests in order to continually produce results, use the URL `http://localhost:5000/rest/pbs/start/difftrans/so/all`. This 
will continually execute a loop of all test cases while trapping any errors. To stop the loop use `http://localhost:5000/rest/pbs/endLoop`

3.) Success! Now you can start generating results. If you have any questions, please contact me at
ravanj1@citadel.edu or fill out an issue through this repository.
