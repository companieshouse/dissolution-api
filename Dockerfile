FROM maven:3-openjdk-11-slim AS builder
WORKDIR /build

COPY pom.xml ./
# RUN mvn dependency:resolve && mvn dependency:resolve-plugins
RUN mvn verify --fail-never

COPY src ./src
RUN mvn package -Dmaven.test.skip=true

## Runtime image
FROM gcr.io/distroless/java:11-debug
WORKDIR /app

COPY --from=builder /build/target/dissolution-api-*.jar /app/dissolution-api.jar

CMD ["/app/dissolution-api.jar"]
