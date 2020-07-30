# Dissolution API

## Technologies
- [OpenJDK 11](https://jdk.java.net/archive/)
- [Maven](https://maven.apache.org/download.cgi)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Swagger OpenAPI](https://swagger.io/docs/specification/about/)

## Running locally

1. Clone [Docker CHS Development](https://github.com/companieshouse/docker-chs-development) and follow the steps in the README.

2. Enable the `dissolution` module

3. Dissolution API can be accessed using `http://api.chs.local:4001/dissolution-request` from within a docker container.

## To make local changes

Development mode is available for this service in [Docker CHS Development](https://github.com/companieshouse/docker-chs-development).

    ./bin/chs-dev development enable dissolution-api

## To build the Docker container

You must be connected to the VPN.

    docker build -t 169942020521.dkr.ecr.eu-west-1.amazonaws.com/local/dissolution-api:latest .

### API Documentation (Swagger)

http://localhost:3001/swagger-ui.html
