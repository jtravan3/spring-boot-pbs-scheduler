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

5.) If all tests succeed and the project can be built successfully, navigate to the Spring Boot
Application with the name `PredictionBasedPrototypeApplication.java`. If you're using IntelliJ,
click on the file and click `Run`.

## Docker

1.) If you want to execute via Docker or use Docker to deploy in the cloud for more computing power, then you can use these
commands to build a docker image.

First, to build an image run the command from the project root directory:

```bash
docker build -t spring-boot-pbs-scheduler .
```

This will build an image from the `Dockerfile`. 

2.) To execute the Docker image run the command.

```bash
docker run -p 5000:5000 spring-boot-pbs-scheduler
```

With the `Dockerfile` and the Amazon S3 configuration, you can deploy the app to the cloud to use more computing power in order
to execute longer tests.

## Docker Hub

I have a personal Docker Hub repository (https://cloud.docker.com/repository/docker/jtravan3/spring-boot-pbs-scheduler) with the latest image. 
You can pull this image down and run it directly. For access, email me directly at ravanj1@citadel.edu.

Once you have access, simply run the command:

```bash
docker pull jtravan3/spring-boot-pbs-scheduler
```

This will pull the latest image to your local machine. Once you have the latest image execute:

```bash
docker run -p 5000:5000 jtravan3/spring-boot-pbs-scheduler
```

You can perform these commands in a pre-built digital ocean container so that your resources 
are not consumed locally.

## Execution

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
