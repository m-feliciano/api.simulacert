package com.simulacert.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.import")
public class ImportProperties {
    private String inputDir = "./data/import/fazer";
    private String processedDir = "./data/import/feito";
}

