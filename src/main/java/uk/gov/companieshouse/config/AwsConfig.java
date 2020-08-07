package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Value("aws.region")
    private String region;

    @Bean
    public S3Client configureS3Client() {
        return S3Client
                .builder()
                .region(Region.of(region))
                .build();
    }
}
