package com.simulacert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SimulaCertApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulaCertApplication.class, args);
    }
}
