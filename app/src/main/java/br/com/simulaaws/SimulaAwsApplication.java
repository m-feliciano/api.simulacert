package br.com.simulaaws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.simulaaws")
public class SimulaAwsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulaAwsApplication.class, args);
    }
}

