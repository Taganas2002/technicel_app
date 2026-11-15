package com.technicel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.technicel.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class TechnicelApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechnicelApplication.class, args);
    }
}
