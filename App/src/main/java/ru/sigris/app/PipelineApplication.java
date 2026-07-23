package ru.sigris.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "ru.sigris.core",
        "ru.sigris.service",
        "ru.sigris.controller"
})
@EntityScan("ru.sigris.model")
@EnableJpaRepositories("ru.sigris.dao")
public class PipelineApplication {
    public static void main(String[] args) {
        SpringApplication.run(PipelineApplication.class, args);
    }
}