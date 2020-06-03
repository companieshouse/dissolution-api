# Dissolution API

## Technologies
- [OpenJDK 11](https://jdk.java.net/archive/)
- [Maven](https://maven.apache.org/download.cgi)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Swagger OpenAPI](https://swagger.io/docs/specification/about/)

### Running locally with Docker  

Required tools:
- [Docker for Mac](https://hub.docker.com/editions/community/docker-ce-desktop-mac)
- [Docker-Compose](https://docs.docker.com/compose/install/)

To bring the environment up, run `docker-compose up` in the project folder. You must be connected to CH VPN in order to download dependencies.

After making local changes to the app, Ctrl+C on the running `docker-compose` terminal session and run `docker-compose up --build` command.

Make local changes to the app, Ctrl/+C on the running `docker-compose` terminal session and re-run the command.

## Useful Endpoints

### Health

http://localhost:3001/actuator/health

### API Documentation (Swagger)

http://localhost:3001/swagger-ui.html
