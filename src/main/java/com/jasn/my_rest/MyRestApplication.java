package com.jasn.my_rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MyRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyRestApplication.class, args);
    }

}
