# Spring Boot Prediction-Based Scheduler Prototype

- Uses Spring Boot 2.0.0-M3 with Spring 5 Framework using Reactor Streams for Reactive REST endpoints
- Uses Spring MVC Style Resource push.

## Setup Instructions

1.) Clone down this repository

2.) Instal MongoDB using Homebrew. For instructions on how to install Homebrew see the setup 
guide located **[here](https://brew.sh/)**

  - First run brew update
  
```bash
brew update
```
  - Then install MongoDB binaries
  
```bash
brew install mongodb
```

  - Create the data directory
  
```bash
mkdir -p /data/db
```
  - Make sure MongoDB has access to the newly created folder
  
```bash
chmod 777 /data/db
```

  - Run MongoDB

```bash
mongod --bind_ip 127.0.0.1
```

3.) Once MongoDB is installed, ensure that all tests run successfully and the project can be built
via Maven.

```bash
mvn install
```

4.) Ensure you have the correct file path set for the output files. These files generally will be
output to the desktop. To change the location change the static variable called `RESEARCH_OUTPUT_FILE_NAME` 
in the file `MetricsAggregator` to the location that you want saved. Right now it is set 
to `/Users/jravan/Desktop/research-output`. This will save 7 files to the Desktop under the names of each
test case (`research-output1.csv`, `research-output2.csv`, etc.)


5.) If all tests succeed and the project can be built successfully, navigate to the Spring Boot
Application with the name `PredictionBasedPrototypeApplication.java`. If you're using IntelliJ,
click on the file and click `Run`.

6.) With the application running navigate to a web browser and access the 
URL `http://localhost:8082/rest/pbs/start/difftrans/so/{testCaseNumber}` where `testCaseNumber`
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


7.) Success! Now you can start generating results. If you have any questions, please contact me at
ravanj1@citadel.edu or fill out an issue through this repository.
