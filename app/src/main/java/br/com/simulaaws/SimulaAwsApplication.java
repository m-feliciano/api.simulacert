package br.com.simulaaws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "br.com.simulaaws")
@EnableFeignClients(basePackages = "br.com.simulaaws.clients")
public class SimulaAwsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulaAwsApplication.class, args);
    }
}

