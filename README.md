# Dissolution API

## Technologies
- [OpenJDK 11](https://jdk.java.net/archive/)
- [Maven](https://maven.apache.org/download.cgi)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Swagger OpenAPI](https://swagger.io/docs/specification/about/)

## Running locally

1. Clone [Docker CHS Development](https://github.com/companieshouse/docker-chs-development) and follow the steps in the README (Development workflow is available for this service)

2. Enable the `dissolution` module

3. Dissolution API can be accessed using `http://api.chs.local:4001/dissolution-request` from within a docker container

## Manually build the Docker container

You must be connected to the VPN

    mvn compile jib:dockerBuild -Dimage=416670754337.dkr.ecr.eu-west-2.amazonaws.com/local/dissolution-api

### API Documentation (Swagger)

http://localhost:3001/swagger-ui.html

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.


Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        |filing-close                                      | ECS cluster (stack) the service belongs to
**Load balancer**      |{env}-chs-apichgovuk & {env}-chs-apichgovuk-private                      | The load balancer that sits in front of the service
**Concourse pipeline**     |[Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/dissolution-api ) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/dissolution-api)                                  | Concourse pipeline link in shared services


### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
