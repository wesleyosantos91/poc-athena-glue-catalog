package io.github.wesleyosantos91;

import io.github.wesleyosantos91.infrastructure.envs.AppProps;
import io.github.wesleyosantos91.infrastructure.envs.AwsProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({AwsProps.class, AppProps.class})
@SpringBootApplication
public class PocAthenaGlueCatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(PocAthenaGlueCatalogApplication.class, args);
    }

}
