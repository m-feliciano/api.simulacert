package com.simulacert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.simulacert")
public class SimulaCertApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulaCertApplication.class, args);
    }
}

