package com.simulacert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// Allow JPA to scan entities and repositories in the base package
@EntityScan(basePackages = "com.simulacert")
// Enable JPA repositories in the base package
@EnableJpaRepositories(basePackages = "com.simulacert")
public class SimulaCertApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulaCertApplication.class, args);
    }
}

