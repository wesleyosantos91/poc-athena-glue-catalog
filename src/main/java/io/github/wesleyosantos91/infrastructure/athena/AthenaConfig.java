package io.github.wesleyosantos91.infrastructure.athena;

import io.github.wesleyosantos91.infrastructure.envs.AwsProps;
import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;

@Configuration
public class AthenaConfig {


    @Bean
    public AthenaClient athenaClient(AwsProps awsProps) {
        final var builder = AthenaClient.builder();

        if (awsProps.getEndpoint() != null && !awsProps.getEndpoint().isEmpty()) {
            builder.endpointOverride(URI.create(awsProps.getEndpoint()));
        }

        return builder.region(Region.of(awsProps.getRegion())).credentialsProvider(DefaultCredentialsProvider.builder().build()).build();
    }
}
